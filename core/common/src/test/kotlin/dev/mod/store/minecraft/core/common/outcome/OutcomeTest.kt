package dev.mod.store.minecraft.core.common.outcome

import dev.mod.store.minecraft.core.common.error.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutcomeTest {

    @Test
    fun `done reports success flags and value`() {
        val outcome: Outcome<Int> = Outcome.Done(42)

        assertTrue(outcome.isDone)
        assertFalse(outcome.isFailed)
        assertEquals(42, outcome.valueOrNull())
    }

    @Test
    fun `failed without fallback reports failure flags and null value`() {
        val outcome: Outcome<Int> = Outcome.Failed(AppError.NetworkError.SERVER)

        assertTrue(outcome.isFailed)
        assertFalse(outcome.isDone)
        assertNull(outcome.valueOrNull())
    }

    @Test
    fun `failed with fallback exposes the fallback value`() {
        val outcome: Outcome<Int> = Outcome.Failed(AppError.NetworkError.NO_CONNECTION, value = 7)

        assertEquals(7, outcome.valueOrNull())
    }

    @Test
    fun `onDone runs only for success`() {
        var seen: Int? = null
        Outcome.Done(5).onDone { seen = it }
        assertEquals(5, seen)

        seen = null
        Outcome.Failed<Int>(AppError.NetworkError.UNKNOWN).onDone { seen = it }
        assertNull(seen)
    }

    @Test
    fun `onFailed runs only for failure and surfaces error plus fallback`() {
        var error: AppError? = null
        var fallback: Int? = null
        Outcome.Failed(AppError.NetworkError.TIMEOUT, value = 9).onFailed { e, f ->
            error = e
            fallback = f
        }
        assertEquals(AppError.NetworkError.TIMEOUT, error)
        assertEquals(9, fallback)

        error = null
        Outcome.Done(1).onFailed { e, _ -> error = e }
        assertNull(error)
    }

    @Test
    fun `map transforms the success value`() {
        val mapped = Outcome.Done(3).map { it * 2 }
        assertEquals(Outcome.Done(6), mapped)
    }

    @Test
    fun `map preserves error and transforms the fallback when present`() {
        val mapped = Outcome.Failed(AppError.NetworkError.SERVER, value = 4).map { it * 2 }

        assertTrue(mapped is Outcome.Failed)
        mapped as Outcome.Failed
        assertEquals(AppError.NetworkError.SERVER, mapped.error)
        assertEquals(8, mapped.value)
    }

    @Test
    fun `map leaves a missing fallback as null`() {
        val mapped = Outcome.Failed<Int>(AppError.NetworkError.SERVER).map { it * 2 }

        mapped as Outcome.Failed
        assertNull(mapped.value)
    }

    @Test
    fun `fold collapses both branches`() {
        val onDone = Outcome.Done(10).fold(onDone = { "ok:$it" }, onFailed = { _, _ -> "err" })
        assertEquals("ok:10", onDone)

        val onFailed = Outcome.Failed<Int>(AppError.NetworkError.NOT_FOUND)
            .fold(onDone = { "ok:$it" }, onFailed = { e, _ -> "err:$e" })
        assertEquals("err:${AppError.NetworkError.NOT_FOUND}", onFailed)
    }
}
