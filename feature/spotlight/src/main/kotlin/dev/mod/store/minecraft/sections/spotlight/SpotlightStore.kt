package dev.mod.store.minecraft.feature.spotlight

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.FetchCreationUseCase
import dev.mod.store.minecraft.domain.bookmark.ToggleBookmarkUseCase
import dev.mod.store.minecraft.domain.outreach.SubmitReportUseCase
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.feature.spotlight.SpotlightStore.Intent
import dev.mod.store.minecraft.feature.spotlight.SpotlightStore.Label
import dev.mod.store.minecraft.feature.spotlight.SpotlightStore.State
import kotlinx.coroutines.launch

private const val MAX_EMAIL_LENGTH = 64
private const val MAX_MESSAGE_LENGTH = 2000

interface SpotlightStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object Retry : Intent
        data object ToggleBookmark : Intent
        data object OpenReport : Intent
        data object DismissReport : Intent
        data class ChangeReportEmail(val value: String) : Intent
        data class ChangeReportMessage(val value: String) : Intent
        data object SubmitReport : Intent
    }

    data class State(
        val creation: CreationEntity? = null,
        val stage: ScreenStage = ScreenStage.Loading,
        val reportOpen: Boolean = false,
        val report: ReportForm = ReportForm(),
    )

    data class ReportForm(
        val email: String = "",
        val message: String = "",
        val sending: Boolean = false,
    ) {
        val canSubmit: Boolean get() = !sending && email.isNotBlank() && message.isNotBlank()
    }

    sealed interface Label {
        data class Notify(val message: String) : Label
        data object ReportSent : Label
    }
}

internal class SpotlightStoreFactory(
    private val storeFactory: StoreFactory,
    private val creationId: Int,
    private val fetchCreation: FetchCreationUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
    private val submitReport: SubmitReportUseCase,
    private val faults: FaultMessages,
) {

    fun create(): SpotlightStore =
        object : SpotlightStore, Store<Intent, State, Label> by storeFactory.create(
            name = "SpotlightStore",
            initialState = State(),
            bootstrapper = coroutineBootstrapper { dispatch(Action.Load) },
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Action {
        data object Load : Action
    }

    private sealed interface Message {
        data object Loading : Message
        data class Loaded(val creation: CreationEntity) : Message
        data class Failed(val message: String) : Message
        data class BookmarkSet(val value: Boolean) : Message
        data class ReportOpen(val value: Boolean) : Message
        data class ReportEmail(val value: String) : Message
        data class ReportMessage(val value: String) : Message
        data class ReportSending(val value: Boolean) : Message
        data object ReportReset : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        Message.Loading -> copy(stage = ScreenStage.Loading)
        is Message.Loaded -> copy(creation = message.creation, stage = ScreenStage.Ready)
        is Message.Failed -> copy(stage = ScreenStage.Failed(message.message))
        is Message.BookmarkSet -> copy(creation = creation?.copy(isBookmarked = message.value))
        is Message.ReportOpen -> copy(reportOpen = message.value)
        is Message.ReportEmail -> copy(report = report.copy(email = message.value))
        is Message.ReportMessage -> copy(report = report.copy(message = message.value))
        is Message.ReportSending -> copy(report = report.copy(sending = message.value))
        Message.ReportReset -> copy(reportOpen = false, report = State().report)
    }

    private inner class Executor :
        CoroutineExecutor<Intent, Action, State, Message, Label>() {

        override fun executeAction(action: Action) {
            when (action) {
                Action.Load -> load()
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                Intent.Retry -> load()
                Intent.ToggleBookmark -> toggle()
                Intent.OpenReport -> dispatch(Message.ReportOpen(true))
                Intent.DismissReport -> dispatch(Message.ReportOpen(false))
                is Intent.ChangeReportEmail ->
                    if (intent.value.length <= MAX_EMAIL_LENGTH) dispatch(Message.ReportEmail(intent.value))
                is Intent.ChangeReportMessage ->
                    if (intent.value.length <= MAX_MESSAGE_LENGTH) dispatch(Message.ReportMessage(intent.value))
                Intent.SubmitReport -> submit()
            }
        }

        private fun load() {
            scope.launch {
                dispatch(Message.Loading)
                when (val outcome = fetchCreation(creationId)) {
                    is Outcome.Done -> dispatch(Message.Loaded(outcome.value))
                    is Outcome.Failed -> {
                        val message = faults.text(outcome.error)
                        dispatch(Message.Failed(message))
                        publish(Label.Notify(message))
                    }
                }
            }
        }

        private fun toggle() {
            val current = state().creation ?: return
            scope.launch {
                val now = toggleBookmark(current.id)
                dispatch(Message.BookmarkSet(now))
            }
        }

        private fun submit() {
            val form = state().report
            if (!form.canSubmit) return
            scope.launch {
                dispatch(Message.ReportSending(true))
                when (val outcome = submitReport(form.email, form.message)) {
                    is Outcome.Done -> {
                        dispatch(Message.ReportReset)
                        publish(Label.ReportSent)
                    }
                    is Outcome.Failed -> {
                        dispatch(Message.ReportSending(false))
                        publish(Label.Notify(faults.text(outcome.error)))
                    }
                }
            }
        }
    }
}
