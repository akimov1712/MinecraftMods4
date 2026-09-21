package dev.mod.store.minecraft.domain.reaction

import dev.mod.store.minecraft.core.common.error.AppError
import dev.mod.store.minecraft.core.common.outcome.Outcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReactionSummaryTest {

    private val base = ReactionSummary(
        selected = null,
        counts = mapOf(ReactionType.LIKE to 3, ReactionType.FIRE to 1),
        total = 4,
    )

    @Test
    fun `adding a reaction counts it and grows the total`() {
        val next = base.choose(ReactionType.LOVE)
        assertEquals(ReactionType.LOVE, next.selected)
        assertEquals(1, next.count(ReactionType.LOVE))
        assertEquals(5, next.total)
    }

    @Test
    fun `switching moves one vote and leaves the total alone`() {
        val liked = base.copy(selected = ReactionType.LIKE)
        val next = liked.choose(ReactionType.FIRE)
        assertEquals(2, next.count(ReactionType.LIKE))
        assertEquals(2, next.count(ReactionType.FIRE))
        assertEquals(4, next.total)
    }

    @Test
    fun `taking a reaction back returns its vote`() {
        val liked = base.copy(selected = ReactionType.LIKE)
        val next = liked.choose(null)
        assertNull(next.selected)
        assertEquals(2, next.count(ReactionType.LIKE))
        assertEquals(3, next.total)
    }

    @Test
    fun `counts never go negative on stale data`() {
        val stale = ReactionSummary(selected = ReactionType.WOW, counts = emptyMap(), total = 0)
        val next = stale.choose(null)
        assertEquals(0, next.count(ReactionType.WOW))
        assertEquals(0, next.total)
    }
}

class SetReactionUseCaseTest {

    private class FakeRepository(var result: Outcome<Unit> = Outcome.Done(Unit)) : ReactionRepository {
        val writes = mutableListOf<ReactionType?>()
        override suspend fun fetch(modId: Int): Outcome<ReactionSummary> = Outcome.Done(ReactionSummary())
        override suspend fun set(modId: Int, reaction: ReactionType?): Outcome<Unit> {
            writes += reaction
            return result
        }
    }

    private class FakeLedger : ReactionLedger {
        val selected = mutableMapOf<Int, ReactionType?>()
        val writes = mutableMapOf<Int, Long>()
        override fun selected(modId: Int) = selected[modId]
        override fun remember(modId: Int, reaction: ReactionType?) { selected[modId] = reaction }
        override fun lastWriteAt(modId: Int) = writes[modId] ?: 0L
        override fun markWrite(modId: Int, atMillis: Long) { writes[modId] = atMillis }
    }

    @Test
    fun `an accepted write is remembered locally`() = runTest {
        val repo = FakeRepository()
        val ledger = FakeLedger()
        val result = SetReactionUseCase(repo, ledger) { 10_000L }(1, ReactionType.FIRE)

        assertEquals(ReactionWrite.Applied, result)
        assertEquals(ReactionType.FIRE, ledger.selected(1))
        assertEquals(listOf(ReactionType.FIRE), repo.writes)
    }

    @Test
    fun `a second write inside the cooldown is refused without a request`() = runTest {
        val repo = FakeRepository()
        val ledger = FakeLedger()
        var now = 10_000L
        val useCase = SetReactionUseCase(repo, ledger) { now }

        useCase(1, ReactionType.FIRE)
        now += 300
        val second = useCase(1, ReactionType.LOVE)

        assertEquals(ReactionWrite.Throttled, second)
        assertEquals(1, repo.writes.size)
        assertEquals(ReactionType.FIRE, ledger.selected(1))
    }

    @Test
    fun `the cooldown is per mod`() = runTest {
        val repo = FakeRepository()
        val useCase = SetReactionUseCase(repo, FakeLedger()) { 10_000L }

        useCase(1, ReactionType.FIRE)
        val other = useCase(2, ReactionType.FIRE)

        assertEquals(ReactionWrite.Applied, other)
        assertEquals(2, repo.writes.size)
    }

    @Test
    fun `a write after the cooldown goes through`() = runTest {
        val repo = FakeRepository()
        var now = 10_000L
        val useCase = SetReactionUseCase(repo, FakeLedger()) { now }

        useCase(1, ReactionType.FIRE)
        now += 5_000
        assertEquals(ReactionWrite.Applied, useCase(1, ReactionType.LOVE))
        assertEquals(2, repo.writes.size)
    }

    @Test
    fun `a write that changes nothing is never sent`() = runTest {
        val repo = FakeRepository()
        val ledger = FakeLedger().apply { selected[1] = ReactionType.FIRE }
        val result = SetReactionUseCase(repo, ledger) { 10_000L }(1, ReactionType.FIRE)

        assertEquals(ReactionWrite.Applied, result)
        assertTrue(repo.writes.isEmpty())
    }

    @Test
    fun `a failed write is not remembered`() = runTest {
        val repo = FakeRepository(Outcome.Failed(AppError.NetworkError.NO_CONNECTION))
        val ledger = FakeLedger()
        val result = SetReactionUseCase(repo, ledger) { 10_000L }(1, ReactionType.FIRE)

        assertTrue(result is ReactionWrite.Failed)
        assertNull(ledger.selected(1))
    }
}

class FetchReactionsUseCaseTest {

    @Test
    fun `offline, the local choice is still returned`() = runTest {
        val repo = object : ReactionRepository {
            override suspend fun fetch(modId: Int): Outcome<ReactionSummary> =
                Outcome.Failed(AppError.NetworkError.NO_CONNECTION)
            override suspend fun set(modId: Int, reaction: ReactionType?) = Outcome.Done(Unit)
        }
        val ledger = object : ReactionLedger {
            override fun selected(modId: Int) = ReactionType.LOVE
            override fun remember(modId: Int, reaction: ReactionType?) = Unit
            override fun lastWriteAt(modId: Int) = 0L
            override fun markWrite(modId: Int, atMillis: Long) = Unit
        }

        val outcome = FetchReactionsUseCase(repo, ledger)(1)
        assertTrue(outcome is Outcome.Failed)
        assertEquals(ReactionType.LOVE, (outcome as Outcome.Failed).value?.selected)
    }
}
