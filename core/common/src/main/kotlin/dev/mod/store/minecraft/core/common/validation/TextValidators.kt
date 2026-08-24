package dev.mod.store.minecraft.core.common.validation

import dev.mod.store.minecraft.core.common.error.AppError.ValidationError
import dev.mod.store.minecraft.core.common.outcome.Outcome

private val EMAIL_PATTERN = Regex(
    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
)

private val LINK_PATTERN = Regex(
    "^https?://[\\w-]+(\\.[\\w-]+)+([\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]*)?$",
)

private fun done(): Outcome<Unit> = Outcome.Done(Unit)

private fun fail(error: ValidationError): Outcome<Unit> = Outcome.Failed(error)

/** Rejects blank input and anything that doesn't look like a plausible email address. */
class EmailValidator : Validator<String> {
    override fun validate(value: String): Outcome<Unit> {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> fail(ValidationError.EmptyInput)
            !EMAIL_PATTERN.matches(trimmed) -> fail(ValidationError.MalformedEmail)
            else -> done()
        }
    }
}

/** Rejects blank input and anything that isn't an http(s) URL. */
class LinkValidator : Validator<String> {
    override fun validate(value: String): Outcome<Unit> {
        val trimmed = value.trim()
        return when {
            trimmed.isEmpty() -> fail(ValidationError.EmptyInput)
            !LINK_PATTERN.matches(trimmed) -> fail(ValidationError.MalformedLink)
            else -> done()
        }
    }
}

/**
 * Bounds the trimmed length of a string. Blank input is reported as
 * [ValidationError.EmptyInput] when a positive [minLength] is required, otherwise as
 * [ValidationError.TooShort] / [ValidationError.TooLong].
 */
class TextLengthValidator(
    private val minLength: Int = 0,
    private val maxLength: Int = Int.MAX_VALUE,
) : Validator<String> {

    init {
        require(minLength >= 0) { "minLength must be >= 0" }
        require(maxLength >= minLength) { "maxLength must be >= minLength" }
    }

    override fun validate(value: String): Outcome<Unit> {
        val length = value.trim().length
        return when {
            length == 0 && minLength > 0 -> fail(ValidationError.EmptyInput)
            length < minLength -> fail(ValidationError.TooShort(minLength))
            length > maxLength -> fail(ValidationError.TooLong(maxLength))
            else -> done()
        }
    }
}
