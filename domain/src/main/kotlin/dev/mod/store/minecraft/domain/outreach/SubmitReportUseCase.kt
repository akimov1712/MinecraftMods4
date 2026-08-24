package dev.mod.store.minecraft.domain.outreach

import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.core.common.validation.EmailValidator
import dev.mod.store.minecraft.core.common.validation.TextLengthValidator

private const val MIN_MESSAGE_LENGTH = 10

/** Validates a bug report then submits it; surfaces validation or network failures uniformly. */
class SubmitReportUseCase(
    private val outreachRepository: OutreachRepository,
) {
    private val emailValidator = EmailValidator()
    private val messageValidator = TextLengthValidator(minLength = MIN_MESSAGE_LENGTH)

    suspend operator fun invoke(email: String, message: String): Outcome<Unit> {
        val emailCheck = emailValidator.validate(email)
        if (emailCheck is Outcome.Failed) return Outcome.Failed(emailCheck.error)

        val messageCheck = messageValidator.validate(message)
        if (messageCheck is Outcome.Failed) return Outcome.Failed(messageCheck.error)

        return outreachRepository.submitReport(
            ReportEntity(email = email.trim(), message = message.trim()),
        )
    }
}
