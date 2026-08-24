package dev.mod.store.minecraft.feature.showcase

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.domain.bookmark.FetchBookmarkedIdsUseCase
import dev.mod.store.minecraft.domain.bookmark.ObserveBookmarkCountUseCase
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationFeed
import dev.mod.store.minecraft.domain.creation.CreationQuery
import dev.mod.store.minecraft.domain.creation.FetchHomeDigestUseCase
import dev.mod.store.minecraft.domain.creation.FetchShowcaseUseCase
import dev.mod.store.minecraft.domain.creation.HomeDigest
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Browse
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Intent
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Label
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 12

/**
 * Home is a stack of editorial sections and nothing else — no sorting, no category picker, no
 * search field; how the catalog is sliced is decided in the admin panel. Opening a section in
 * full swaps the sections out for one endless list over that same slice ([State.browse]); the
 * digest is kept in memory the whole time so closing the list is instant.
 */
interface ShowcaseStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class OpenFeed(val feed: CreationFeed) : Intent
        data object CloseBrowse : Intent
        data object Refresh : Intent
        data object LoadMore : Intent
    }

    data class Browse(
        val feed: CreationFeed,
        val items: List<CreationEntity> = emptyList(),
        val total: Int = 0,
        val stage: ScreenStage = ScreenStage.Loading,
        val endReached: Boolean = false,
    )

    data class State(
        val stage: ScreenStage = ScreenStage.Loading,
        val digest: HomeDigest = HomeDigest(),
        val bookmarkCount: Int = 0,
        val browse: Browse? = null,
        val refreshing: Boolean = false,
    )

    sealed interface Label {
        data class Warn(val message: String) : Label
    }
}

internal class ShowcaseStoreFactory(
    private val storeFactory: StoreFactory,
    private val fetchHomeDigest: FetchHomeDigestUseCase,
    private val fetchShowcase: FetchShowcaseUseCase,
    private val observeBookmarkCount: ObserveBookmarkCountUseCase,
    private val fetchBookmarkedIds: FetchBookmarkedIdsUseCase,
    private val faults: FaultMessages,
) {

    fun create(): ShowcaseStore =
        object : ShowcaseStore, Store<Intent, State, Label> by storeFactory.create(
            name = "ShowcaseStore",
            initialState = State(),
            bootstrapper = coroutineBootstrapper { dispatch(Action.Start) },
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Action {
        data object Start : Action
    }

    private sealed interface Message {
        data object DigestLoading : Message
        data object Refreshing : Message
        data class DigestLoaded(val digest: HomeDigest) : Message
        data class DigestFailed(val message: String) : Message
        data class BookmarksChanged(val count: Int, val ids: Set<Int>) : Message
        data class BrowseOpened(val browse: Browse) : Message
        data object BrowseClosed : Message
        data object BrowseLoading : Message
        data class BrowsePage(val items: List<CreationEntity>, val total: Int, val reset: Boolean) : Message
        data class BrowseFailed(val message: String) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        Message.DigestLoading -> copy(stage = ScreenStage.Loading)
        Message.Refreshing -> copy(refreshing = true)
        is Message.DigestLoaded -> copy(
            digest = message.digest,
            stage = ScreenStage.Ready,
            refreshing = false,
        )
        is Message.DigestFailed -> copy(
            stage = if (digest.isEmpty) ScreenStage.Failed(message.message) else ScreenStage.Ready,
            refreshing = false,
        )
        is Message.BookmarksChanged -> copy(
            bookmarkCount = message.count,
            digest = digest.reflag(message.ids),
            browse = browse?.copy(items = browse.items.reflag(message.ids)),
        )
        is Message.BrowseOpened -> copy(browse = message.browse)
        Message.BrowseClosed -> copy(browse = null)
        Message.BrowseLoading -> copy(browse = browse?.copy(stage = ScreenStage.Loading))
        is Message.BrowsePage -> copy(
            refreshing = false,
            browse = browse?.let { current ->
                val items = if (message.reset) message.items else current.items + message.items
                current.copy(
                    items = items,
                    total = message.total,
                    stage = ScreenStage.Ready,
                    endReached = message.items.isEmpty() || items.size >= message.total,
                )
            },
        )
        is Message.BrowseFailed -> copy(
            refreshing = false,
            browse = browse?.copy(stage = ScreenStage.Failed(message.message)),
        )
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        private var digestJob: Job? = null
        private var browseJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.Start -> {
                    loadDigest(refresh = false)
                    observeBookmarkCount()
                        .onEach { count -> dispatch(Message.BookmarksChanged(count, fetchBookmarkedIds())) }
                        .launchIn(scope)
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.OpenFeed -> {
                    dispatch(Message.BrowseOpened(Browse(feed = intent.feed)))
                    loadBrowsePage(reset = true)
                }

                Intent.CloseBrowse -> {
                    browseJob?.cancel()
                    dispatch(Message.BrowseClosed)
                }

                Intent.Refresh -> if (state().browse != null) {
                    dispatch(Message.Refreshing)
                    loadBrowsePage(reset = true)
                } else {
                    loadDigest(refresh = true)
                }

                Intent.LoadMore -> loadBrowsePage(reset = false)
            }
        }

        private fun loadDigest(refresh: Boolean) {
            digestJob?.cancel()
            digestJob = scope.launch {
                dispatch(if (refresh) Message.Refreshing else Message.DigestLoading)
                when (val outcome = fetchHomeDigest()) {
                    is Outcome.Done -> dispatch(Message.DigestLoaded(outcome.value))
                    is Outcome.Failed -> {
                        val message = faults.text(outcome.error)
                        dispatch(Message.DigestFailed(message))
                        if (!state().digest.isEmpty) publish(Label.Warn(message))
                    }
                }
            }
        }

        private fun loadBrowsePage(reset: Boolean) {
            val browse = state().browse ?: return
            if (!reset && (browse.stage.isLoading || browse.endReached)) return

            browseJob?.cancel()
            browseJob = scope.launch {
                dispatch(Message.BrowseLoading)
                val current = state().browse ?: return@launch
                val query = CreationQuery(
                    feed = current.feed,
                    offset = if (reset) 0 else current.items.size,
                    limit = PAGE_SIZE,
                )
                when (val outcome = fetchShowcase(query)) {
                    is Outcome.Done -> dispatch(
                        Message.BrowsePage(outcome.value.items, outcome.value.total, reset),
                    )
                    is Outcome.Failed -> {
                        val message = faults.text(outcome.error)
                        dispatch(Message.BrowseFailed(message))
                        publish(Label.Warn(message))
                    }
                }
            }
        }
    }
}

private fun List<CreationEntity>.reflag(bookmarked: Set<Int>): List<CreationEntity> =
    map { creation -> creation.copy(isBookmarked = creation.id in bookmarked) }

private fun HomeDigest.reflag(bookmarked: Set<Int>): HomeDigest = copy(
    pickOfDay = pickOfDay?.let { pick -> pick.copy(isBookmarked = pick.id in bookmarked) },
    trending = trending.reflag(bookmarked),
    popular = popular.reflag(bookmarked),
    topRated = topRated.reflag(bookmarked),
    fresh = fresh.reflag(bookmarked),
)
