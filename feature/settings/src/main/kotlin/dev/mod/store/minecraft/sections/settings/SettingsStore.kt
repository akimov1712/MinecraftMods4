package dev.mod.store.minecraft.feature.settings

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.domain.loadout.ClearDownloadedFilesUseCase
import dev.mod.store.minecraft.domain.loadout.CountDownloadedFilesUseCase
import dev.mod.store.minecraft.feature.settings.SettingsStore.Intent
import dev.mod.store.minecraft.feature.settings.SettingsStore.Label
import dev.mod.store.minecraft.feature.settings.SettingsStore.State
import kotlinx.coroutines.launch

/**
 * The little state the settings screen owns: how many files are on the phone, and whether we are
 * asking before throwing them away.
 *
 * The figure is counted on demand rather than observed — files arrive while this screen is in the
 * background and nothing tells it — so it is re-read every time the tab comes back into view.
 */
interface SettingsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        /** Re-count the files on the phone; sent whenever the screen comes back into view. */
        data object Refresh : Intent
        data object AskClear : Intent
        data object DismissClear : Intent
        data object ConfirmClear : Intent
    }

    data class State(
        val downloadedCount: Int = 0,
        val clearRequested: Boolean = false,
    )

    sealed interface Label {
        data object Cleared : Label
    }
}

internal class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    private val countDownloadedFiles: CountDownloadedFilesUseCase,
    private val clearDownloadedFiles: ClearDownloadedFilesUseCase,
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
        data class DownloadedCount(val value: Int) : Message
        data class ClearRequested(val value: Boolean) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.DownloadedCount -> copy(downloadedCount = message.value)
        is Message.ClearRequested -> copy(clearRequested = message.value)
    }

    private inner class Executor : CoroutineExecutor<Intent, Action, State, Message, Label>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.Start -> countFiles()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Refresh -> countFiles()
                Intent.AskClear -> dispatch(Message.ClearRequested(true))
                Intent.DismissClear -> dispatch(Message.ClearRequested(false))
                Intent.ConfirmClear -> scope.launch {
                    clearDownloadedFiles()
                    dispatch(Message.ClearRequested(false))
                    dispatch(Message.DownloadedCount(countDownloadedFiles()))
                    publish(Label.Cleared)
                }
            }
        }

        private fun countFiles() {
            scope.launch { dispatch(Message.DownloadedCount(countDownloadedFiles())) }
        }
    }
}
