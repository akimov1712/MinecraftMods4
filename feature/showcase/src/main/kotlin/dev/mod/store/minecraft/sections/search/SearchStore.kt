package dev.mod.store.minecraft.feature.search

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationQuery
import dev.mod.store.minecraft.domain.creation.FetchHomeDigestUseCase
import dev.mod.store.minecraft.domain.creation.FetchShowcaseUseCase
import dev.mod.store.minecraft.domain.creation.HomeDigest
import dev.mod.store.minecraft.feature.search.SearchStore.Intent
import dev.mod.store.minecraft.feature.search.SearchStore.Label
import dev.mod.store.minecraft.feature.search.SearchStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 12
private const val DEBOUNCE_MS = 350L

/**
 * Search over the whole catalog. With an empty field the screen is not blank: it shows the same
 * editorial sections as home, so there is always something to tap.
 */
interface SearchStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class SetQuery(val value: String) : Intent
        data object Clear : Intent
        data object LoadMore : Intent
        data object Retry : Intent
    }

    data class State(
        val query: String = "",
        val results: List<CreationEntity> = emptyList(),
        val total: Int = 0,
        val stage: ScreenStage = ScreenStage.Idle,
        val endReached: Boolean = false,
        val suggestions: HomeDigest = HomeDigest(),
        val suggestionsLoading: Boolean = true,
    )

    sealed interface Label {
        data class Warn(val message: String) : Label
    }
}

internal class SearchStoreFactory(
    private val storeFactory: StoreFactory,
    private val fetchShowcase: FetchShowcaseUseCase,
    private val fetchHomeDigest: FetchHomeDigestUseCase,
    private val faults: FaultMessages,
) {

    fun create(): SearchStore =
        object : SearchStore, Store<Intent, State, Label> by storeFactory.create(
            name = "SearchStore",
            initialState = State(),
            bootstrapper = coroutineBootstrapper { dispatch(Action.Start) },
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Action {
        data object Start : Action
    }

    private sealed interface Message {
        data class QuerySet(val value: String) : Message
        data object Cleared : Message
        data object Loading : Message
        data class Page(val items: List<CreationEntity>, val total: Int, val reset: Boolean) : Message
        data class Failed(val message: String) : Message
        data class SuggestionsLoaded(val digest: HomeDigest) : Message
        data object SuggestionsFailed : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.QuerySet -> copy(query = message.value)
        Message.Cleared -> copy(
            query = "",
            results = emptyList(),
            total = 0,
            stage = ScreenStage.Idle,
            endReached = false,
        )
        Message.Loading -> copy(stage = ScreenStage.Loading)
        is Message.Page -> {
            val items = if (message.reset) message.items else results + message.items
            copy(
                results = items,
                total = message.total,
                stage = ScreenStage.Ready,
                endReached = message.items.isEmpty() || items.size >= message.total,
            )
        }
        is Message.Failed -> copy(stage = ScreenStage.Failed(message.message))
        is Message.SuggestionsLoaded -> copy(suggestions = message.digest, suggestionsLoading = false)
        Message.SuggestionsFailed -> copy(suggestionsLoading = false)
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        private var searchJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.Start -> scope.launch {
                    when (val outcome = fetchHomeDigest()) {
                        is Outcome.Done -> dispatch(Message.SuggestionsLoaded(outcome.value))
                        is Outcome.Failed -> dispatch(Message.SuggestionsFailed)
                    }
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SetQuery -> {
                    dispatch(Message.QuerySet(intent.value))
                    if (intent.value.isBlank()) {
                        searchJob?.cancel()
                        dispatch(Message.Cleared)
                    } else {
                        search(reset = true, debounce = true)
                    }
                }

                Intent.Clear -> {
                    searchJob?.cancel()
                    dispatch(Message.Cleared)
                }

                Intent.LoadMore -> search(reset = false, debounce = false)

                Intent.Retry -> search(reset = true, debounce = false)
            }
        }

        private fun search(reset: Boolean, debounce: Boolean) {
            val snapshot = state()
            if (snapshot.query.isBlank()) return
            if (!reset && (snapshot.stage.isLoading || snapshot.endReached)) return

            searchJob?.cancel()
            searchJob = scope.launch {
                if (debounce) delay(DEBOUNCE_MS)
                dispatch(Message.Loading)
                val current = state()
                val query = CreationQuery(
                    searchText = current.query.trim(),
                    offset = if (reset) 0 else current.results.size,
                    limit = PAGE_SIZE,
                )
                when (val outcome = fetchShowcase(query)) {
                    is Outcome.Done -> dispatch(
                        Message.Page(outcome.value.items, outcome.value.total, reset),
                    )
                    is Outcome.Failed -> {
                        val message = faults.text(outcome.error)
                        dispatch(Message.Failed(message))
                        publish(Label.Warn(message))
                    }
                }
            }
        }
    }
}
