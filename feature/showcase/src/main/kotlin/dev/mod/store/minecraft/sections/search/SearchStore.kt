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
import dev.mod.store.minecraft.domain.creation.FetchShowcaseUseCase
import dev.mod.store.minecraft.feature.search.SearchStore.Intent
import dev.mod.store.minecraft.feature.search.SearchStore.Label
import dev.mod.store.minecraft.feature.search.SearchStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 12
private const val DEBOUNCE_MS = 350L

/**
 * Search over the whole catalog.
 *
 * An empty field is not an empty screen: it lists the catalog in the house order, and that list
 * pages exactly like a search does. It used to be a fixed slice of ten from the home digest, which
 * simply stopped at the tenth mod however far anyone scrolled.
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
        val stage: ScreenStage = ScreenStage.Loading,
        val endReached: Boolean = false,
    )

    sealed interface Label {
        data class Warn(val message: String) : Label
    }
}

internal class SearchStoreFactory(
    private val storeFactory: StoreFactory,
    private val fetchShowcase: FetchShowcaseUseCase,
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
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.QuerySet -> copy(query = message.value)
        Message.Cleared -> copy(query = "", results = emptyList(), total = 0, endReached = false)
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
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        private var searchJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.Start -> search(reset = true, debounce = false)
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SetQuery -> {
                    dispatch(Message.QuerySet(intent.value))
                    // An emptied field falls back to the plain catalog rather than to a blank page.
                    search(reset = true, debounce = intent.value.isNotBlank())
                }

                Intent.Clear -> {
                    dispatch(Message.Cleared)
                    search(reset = true, debounce = false)
                }

                Intent.LoadMore -> search(reset = false, debounce = false)

                Intent.Retry -> search(reset = true, debounce = false)
            }
        }

        private fun search(reset: Boolean, debounce: Boolean) {
            val snapshot = state()
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
