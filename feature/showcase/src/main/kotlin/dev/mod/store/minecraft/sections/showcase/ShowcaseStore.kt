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
import dev.mod.store.minecraft.domain.creation.CreationCategory
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationFeed
import dev.mod.store.minecraft.domain.creation.CreationQuery
import dev.mod.store.minecraft.domain.creation.FetchShowcaseUseCase
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Intent
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Label
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 12

/**
 * Home is one list of the catalog, and the reader decides how it is sliced: a kind of content and
 * an order, both at the top of the page. Nothing else — no editorial sections, no rails, no search
 * field (search has its own key in the bar below).
 *
 * Changing either control restarts the list from the first page; scrolling to the foot asks for the
 * next one. Bookmark state is merged in from a separate stream, so starring a mod elsewhere shows
 * up here without a reload.
 */
interface ShowcaseStore : Store<Intent, State, Label> {

    sealed interface Intent {
        /** Null means every kind of content. */
        data class ChangeCategory(val category: CreationCategory?) : Intent
        data class ChangeSort(val sort: CreationFeed) : Intent
        data object Refresh : Intent
        data object LoadMore : Intent
    }

    data class State(
        val items: List<CreationEntity> = emptyList(),
        val total: Int = 0,
        val category: CreationCategory? = null,
        val sort: CreationFeed = CreationFeed.Trending,
        val stage: ScreenStage = ScreenStage.Loading,
        val endReached: Boolean = false,
        val refreshing: Boolean = false,
    )

    sealed interface Label {
        data class Warn(val message: String) : Label
    }
}

internal class ShowcaseStoreFactory(
    private val storeFactory: StoreFactory,
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
        data object Loading : Message
        data object Refreshing : Message
        data class Page(val items: List<CreationEntity>, val total: Int, val reset: Boolean) : Message
        data class Failed(val message: String) : Message
        data class SliceSet(val category: CreationCategory?, val sort: CreationFeed) : Message
        data class BookmarksChanged(val ids: Set<Int>) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        Message.Loading -> copy(stage = ScreenStage.Loading)
        Message.Refreshing -> copy(refreshing = true)
        is Message.Page -> {
            val merged = if (message.reset) message.items else items + message.items
            copy(
                items = merged,
                total = message.total,
                stage = ScreenStage.Ready,
                endReached = message.items.isEmpty() || merged.size >= message.total,
                refreshing = false,
            )
        }
        is Message.Failed -> copy(
            // A failed page never blanks a list that is already on screen.
            stage = if (items.isEmpty()) ScreenStage.Failed(message.message) else ScreenStage.Ready,
            refreshing = false,
        )
        is Message.SliceSet -> copy(
            category = message.category,
            sort = message.sort,
            endReached = false,
        )
        is Message.BookmarksChanged -> copy(items = items.reflag(message.ids))
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        private var pageJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.Start -> {
                    load(reset = true)
                    observeBookmarkCount()
                        .onEach { dispatch(Message.BookmarksChanged(fetchBookmarkedIds())) }
                        .launchIn(scope)
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.ChangeCategory -> {
                    if (intent.category == state().category) return
                    // Newest-first comes from its own endpoint, and that endpoint ignores the
                    // category filter: asking for both would put addons under a "Maps" heading.
                    // Choosing a kind therefore falls back to the default order, visibly, rather
                    // than quietly showing the wrong list.
                    val sort = state().sort
                    dispatch(
                        Message.SliceSet(
                            category = intent.category,
                            sort = if (intent.category != null && sort == CreationFeed.Fresh) {
                                CreationFeed.Trending
                            } else {
                                sort
                            },
                        ),
                    )
                    load(reset = true)
                }

                is Intent.ChangeSort -> {
                    if (intent.sort == state().sort) return
                    // The other half of the same rule: newest-first can only be shown for the
                    // whole catalogue.
                    dispatch(
                        Message.SliceSet(
                            category = if (intent.sort == CreationFeed.Fresh) null else state().category,
                            sort = intent.sort,
                        ),
                    )
                    load(reset = true)
                }

                Intent.Refresh -> {
                    dispatch(Message.Refreshing)
                    load(reset = true)
                }

                Intent.LoadMore -> load(reset = false)
            }
        }

        private fun load(reset: Boolean) {
            val current = state()
            if (!reset && (current.stage.isLoading || current.endReached)) return

            pageJob?.cancel()
            pageJob = scope.launch {
                if (!current.refreshing) dispatch(Message.Loading)
                val query = CreationQuery(
                    category = current.category,
                    feed = current.sort,
                    offset = if (reset) 0 else current.items.size,
                    limit = PAGE_SIZE,
                )
                when (val outcome = fetchShowcase(query)) {
                    is Outcome.Done ->
                        dispatch(Message.Page(outcome.value.items, outcome.value.total, reset))

                    is Outcome.Failed -> {
                        val message = faults.text(outcome.error)
                        dispatch(Message.Failed(message))
                        if (state().items.isNotEmpty()) publish(Label.Warn(message))
                    }
                }
            }
        }
    }
}

private fun List<CreationEntity>.reflag(bookmarked: Set<Int>): List<CreationEntity> =
    map { creation -> creation.copy(isBookmarked = creation.id in bookmarked) }
