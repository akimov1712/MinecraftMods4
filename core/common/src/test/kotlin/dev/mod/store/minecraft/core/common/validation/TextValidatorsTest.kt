package dev.mod.store.minecraft.core.common.validation

import dev.mod.store.minecraft.core.common.error.AppError.ValidationError
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.core.common.outcome.isDone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextValidatorsTest {

    private fun errorOf(outcome: Outcome<Unit>): ValidationError {
        assertTrue("expected a failure but was $outcome", outcome is Outcome.Failed)
        return (outcome as Outcome.Failed).error as ValidationError
    }

    // --- EmailValidator ---

    @Test
    fun `valid email passes`() {
        assertTrue(EmailValidator().validate("Player_99@mods.example.com").isDone)
    }

    @Test
    fun `blank email is empty input`() {
        assertEquals(ValidationError.EmptyInput, errorOf(EmailValidator().validate("   ")))
    }

    @Test
    fun `email without domain is malformed`() {
        assertEquals(ValidationError.MalformedEmail, errorOf(EmailValidator().validate("player@host")))
    }

    @Test
    fun `email without at sign is malformed`() {
        assertEquals(ValidationError.MalformedEmail, errorOf(EmailValidator().validate("player.host.com")))
    }

    // --- LinkValidator ---

    @Test
    fun `valid https link passes`() {
        assertTrue(LinkValidator().validate("https://l13dev.ru/v1/mod/60").isDone)
    }

    @Test
    fun `valid http link passes`() {
        assertTrue(LinkValidator().validate("http://example.com").isDone)
    }

    @Test
    fun `blank link is empty input`() {
        assertEquals(ValidationError.EmptyInput, errorOf(LinkValidator().validate("")))
    }

    @Test
    fun `link without scheme is malformed`() {
        assertEquals(ValidationError.MalformedLink, errorOf(LinkValidator().validate("example.com")))
    }

    @Test
    fun `ftp link is malformed`() {
        assertEquals(ValidationError.MalformedLink, errorOf(LinkValidator().validate("ftp://example.com")))
    }

    // --- TextLengthValidator ---

    @Test
    fun `text within bounds passes`() {
        assertTrue(TextLengthValidator(minLength = 3, maxLength = 10).validate("hello").isDone)
    }

    @Test
    fun `blank required text is empty input`() {
        val outcome = TextLengthValidator(minLength = 5).validate("   ")
        assertEquals(ValidationError.EmptyInput, errorOf(outcome))
    }

    @Test
    fun `too short text reports minimum`() {
        val outcome = TextLengthValidator(minLength = 5).validate("hey")
        assertEquals(ValidationError.TooShort(5), errorOf(outcome))
    }

    @Test
    fun `too long text reports maximum`() {
        val outcome = TextLengthValidator(maxLength = 4).validate("overflow")
        assertEquals(ValidationError.TooLong(4), errorOf(outcome))
    }

    @Test
    fun `length is measured after trimming`() {
        assertTrue(TextLengthValidator(minLength = 2, maxLength = 4).validate("  ok  ").isDone)
    }

    @Test
    fun `empty optional text passes when no minimum is required`() {
        assertTrue(TextLengthValidator(maxLength = 10).validate("").isDone)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `inverted bounds are rejected at construction`() {
        TextLengthValidator(minLength = 10, maxLength = 2)
    }
}
