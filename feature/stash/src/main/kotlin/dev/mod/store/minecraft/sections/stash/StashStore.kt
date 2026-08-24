package dev.mod.store.minecraft.feature.stash

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.domain.bookmark.FetchBookmarkedCreationsUseCase
import dev.mod.store.minecraft.domain.bookmark.ObserveBookmarkCountUseCase
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.feature.stash.StashStore.Intent
import dev.mod.store.minecraft.feature.stash.StashStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 12

interface StashStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data object Refresh : Intent
        data object LoadMore : Intent
    }

    data class State(
        val creations: List<CreationEntity> = emptyList(),
        val count: Int = 0,
        val stage: ScreenStage = ScreenStage.Loading,
        val endReached: Boolean = false,
    )
}

internal class StashStoreFactory(
    private val storeFactory: StoreFactory,
    private val observeCount: ObserveBookmarkCountUseCase,
    private val fetchBookmarked: FetchBookmarkedCreationsUseCase,
    private val faults: FaultMessages,
) {

    fun create(): StashStore =
        object : StashStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "StashStore",
            initialState = State(),
            bootstrapper = coroutineBootstrapper {
                launch { observeCount().collect { dispatch(Action.CountChanged(it)) } }
            },
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Action {
        data class CountChanged(val count: Int) : Action
    }

    private sealed interface Message {
        data class CountSet(val count: Int) : Message
        data object Reset : Message
        data object Loading : Message
        data class Page(val items: List<CreationEntity>) : Message
        data class Failed(val message: String) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.CountSet -> copy(count = message.count)
        Message.Reset -> copy(creations = emptyList(), endReached = false, stage = ScreenStage.Loading)
        Message.Loading -> copy(stage = ScreenStage.Loading)
        is Message.Page -> copy(
            creations = creations + message.items,
            endReached = message.items.isEmpty(),
            stage = ScreenStage.Ready,
        )
        is Message.Failed -> copy(stage = ScreenStage.Failed(message.message))
    }

    private inner class Executor :
        CoroutineExecutor<Intent, Action, State, Message, Nothing>() {

        private var loadJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                is Action.CountChanged -> {
                    dispatch(Message.CountSet(action.count))
                    reload(reset = true)
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Refresh -> reload(reset = true)
                Intent.LoadMore -> reload(reset = false)
            }
        }

        private fun reload(reset: Boolean) {
            val snapshot = state()
            if (!reset && (snapshot.stage.isLoading || snapshot.endReached)) return

            loadJob?.cancel()
            loadJob = scope.launch {
                if (reset) dispatch(Message.Reset)
                dispatch(Message.Loading)
                val offset = if (reset) 0 else state().creations.size
                when (val outcome = fetchBookmarked(offset = offset, limit = PAGE_SIZE)) {
                    is Outcome.Done -> dispatch(Message.Page(outcome.value))
                    is Outcome.Failed -> dispatch(Message.Failed(faults.text(outcome.error)))
                }
            }
        }
    }
}
