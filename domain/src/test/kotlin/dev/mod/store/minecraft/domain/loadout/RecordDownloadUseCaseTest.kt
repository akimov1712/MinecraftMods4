package dev.mod.store.minecraft.domain.loadout

import dev.mod.store.minecraft.core.common.error.AppError
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationPage
import dev.mod.store.minecraft.domain.creation.CreationQuery
import dev.mod.store.minecraft.domain.creation.CreationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordDownloadUseCaseTest {

    private class FakeRepository(var result: Outcome<Int> = Outcome.Done(42)) : CreationRepository {
        var calls = 0
        override suspend fun recordDownload(modId: Int): Outcome<Int> {
            calls++
            return result
        }
        override suspend fun fetchCatalog(query: CreationQuery): Outcome<CreationPage> = error("unused")
        override suspend fun fetchCreation(id: Int): Outcome<CreationEntity> = error("unused")
        override suspend fun fetchPickOfDay(): Outcome<CreationEntity> = error("unused")
        override suspend fun fetchFileSize(url: String): Outcome<Long> = error("unused")
    }

    private class FakeLedger : DownloadLedger {
        val counted = mutableSetOf<Int>()
        override fun isCounted(modId: Int) = modId in counted
        override fun markCounted(modId: Int) { counted += modId }
        override fun unmark(modId: Int) { counted -= modId }
    }

    @Test
    fun `the first download of a mod is counted`() = runTest {
        val repo = FakeRepository()
        val outcome = RecordDownloadUseCase(repo, FakeLedger())(7)

        assertEquals(Outcome.Done(42), outcome)
        assertEquals(1, repo.calls)
    }

    @Test
    fun `a second file of the same mod is not counted again`() = runTest {
        val repo = FakeRepository()
        val useCase = RecordDownloadUseCase(repo, FakeLedger())

        useCase(7)
        val second = useCase(7)

        assertNull(second)
        assertEquals(1, repo.calls)
    }

    @Test
    fun `different mods are counted separately`() = runTest {
        val repo = FakeRepository()
        val useCase = RecordDownloadUseCase(repo, FakeLedger())

        useCase(7)
        useCase(8)

        assertEquals(2, repo.calls)
    }

    @Test
    fun `a failed request releases the claim so a later download can count`() = runTest {
        val repo = FakeRepository(Outcome.Failed(AppError.NetworkError.NO_CONNECTION))
        val ledger = FakeLedger()
        val useCase = RecordDownloadUseCase(repo, ledger)

        useCase(7)
        assertFalse(ledger.isCounted(7))

        repo.result = Outcome.Done(43)
        useCase(7)
        assertTrue(ledger.isCounted(7))
        assertEquals(2, repo.calls)
    }
}
