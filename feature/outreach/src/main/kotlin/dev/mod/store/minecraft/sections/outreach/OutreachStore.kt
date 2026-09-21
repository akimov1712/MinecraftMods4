package dev.mod.store.minecraft.feature.outreach

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.domain.outreach.SubmitRecommendationUseCase
import dev.mod.store.minecraft.domain.outreach.SubmitReportUseCase
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.feature.outreach.OutreachStore.Intent
import dev.mod.store.minecraft.feature.outreach.OutreachStore.Label
import dev.mod.store.minecraft.feature.outreach.OutreachStore.State
import kotlinx.coroutines.launch

private const val MAX_EMAIL_LENGTH = 64

/** The screen shows a live counter against this, so it is not private to the store. */
const val MAX_MESSAGE_LENGTH = 2000

/** The two purposes of the form. */
enum class OutreachMode { Recommendation, Report }

interface OutreachStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class SelectMode(val mode: OutreachMode) : Intent
        data class ChangeEmail(val value: String) : Intent
        data class ChangeMessage(val value: String) : Intent
        data object Submit : Intent

        /** Leave the "sent" confirmation and write another message. */
        data object Compose : Intent
    }

    data class State(
        val mode: OutreachMode = OutreachMode.Recommendation,
        val email: String = "",
        val message: String = "",
        val sending: Boolean = false,
        /** True once a message went through — the screen swaps the form for a confirmation. */
        val sent: Boolean = false,
    ) {
        val canSubmit: Boolean get() = !sending && email.isNotBlank() && message.isNotBlank()
    }

    sealed interface Label {
        data class Notify(val message: String) : Label
        data object Sent : Label
    }
}

internal class OutreachStoreFactory(
    private val storeFactory: StoreFactory,
    private val submitRecommendation: SubmitRecommendationUseCase,
    private val submitReport: SubmitReportUseCase,
    private val faults: FaultMessages,
) {

    fun create(): OutreachStore =
        object : OutreachStore, Store<Intent, State, Label> by storeFactory.create(
            name = "OutreachStore",
            initialState = State(),
            executorFactory = { Executor() },
            reducer = Reducer { message -> reduce(message) },
        ) {}

    private sealed interface Message {
        data class ModeSet(val mode: OutreachMode) : Message
        data class EmailSet(val value: String) : Message
        data class MessageSet(val value: String) : Message
        data class Sending(val value: Boolean) : Message
        data object Cleared : Message
        data class SentSet(val value: Boolean) : Message
    }

    private fun State.reduce(message: Message): State = when (message) {
        is Message.ModeSet -> copy(mode = message.mode)
        is Message.EmailSet -> copy(email = message.value)
        is Message.MessageSet -> copy(message = message.value)
        is Message.Sending -> copy(sending = message.value)
        Message.Cleared -> copy(email = "", message = "", sending = false)
        is Message.SentSet -> copy(sent = message.value)
    }

    private inner class Executor :
        CoroutineExecutor<Intent, Nothing, State, Message, Label>() {

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.SelectMode -> dispatch(Message.ModeSet(intent.mode))
                is Intent.ChangeEmail ->
                    if (intent.value.length <= MAX_EMAIL_LENGTH) dispatch(Message.EmailSet(intent.value))
                is Intent.ChangeMessage ->
                    if (intent.value.length <= MAX_MESSAGE_LENGTH) dispatch(Message.MessageSet(intent.value))
                Intent.Submit -> submit()
                Intent.Compose -> dispatch(Message.SentSet(false))
            }
        }

        private fun submit() {
            val current = state()
            if (!current.canSubmit) return
            scope.launch {
                dispatch(Message.Sending(true))
                val outcome = when (current.mode) {
                    OutreachMode.Recommendation -> submitRecommendation(current.email, current.message)
                    OutreachMode.Report -> submitReport(current.email, current.message)
                }
                when (outcome) {
                    is Outcome.Done -> {
                        dispatch(Message.Cleared)
                        dispatch(Message.SentSet(true))
                        publish(Label.Sent)
                    }
                    is Outcome.Failed -> {
                        dispatch(Message.Sending(false))
                        publish(Label.Notify(faults.text(outcome.error)))
                    }
                }
            }
        }
    }
}
