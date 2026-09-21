package dev.mod.store.minecraft.feature.settings

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.domain.bookmark.ClearBookmarksUseCase
import dev.mod.store.minecraft.domain.bookmark.ObserveBookmarkCountUseCase
import dev.mod.store.minecraft.feature.settings.SettingsStore.Intent
import dev.mod.store.minecraft.feature.settings.SettingsStore.Label
import dev.mod.store.minecraft.feature.settings.SettingsStore.State
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** The little state the settings screen owns: how much is saved, and whether we are asking. */
interface SettingsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object AskClear : Intent
        data object DismissClear : Intent
        data object ConfirmClear : Intent
    }

    data class State(
        val savedCount: Int = 0,
        val clearRequested: Boolean = false,
    )

    sealed interface Label {
        data object Cleared : Label
    }
}

internal class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    private val observeBookmarkCount: ObserveBookmarkCountUseCase,
    private val clearBookmarks: ClearBookmarksUseCase,
) {

    fun create(): SettingsStore =
        object : SettingsStore, Store<Intent, State, Label> by storeFactory.create(
            name = "SettingsStore",
            initialState = State(),
            bootstrapper = coroutineBootstrapper { dispatch(Action.Start) },
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Action {
        data object Start : Action
    }

    private sealed interface Message {
        data class SavedCount(val value: Int) : Message
        data class ClearRequested(val value: Boolean) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.SavedCount -> copy(savedCount = message.value)
        is Message.ClearRequested -> copy(clearRequested = message.value)
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.Start -> observeBookmarkCount()
                    .onEach { count -> dispatch(Message.SavedCount(count)) }
                    .launchIn(scope)
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.AskClear -> dispatch(Message.ClearRequested(true))
                Intent.DismissClear -> dispatch(Message.ClearRequested(false))
                Intent.ConfirmClear -> scope.launch {
                    clearBookmarks()
                    dispatch(Message.ClearRequested(false))
                    publish(Label.Cleared)
                }
            }
        }
    }
}
