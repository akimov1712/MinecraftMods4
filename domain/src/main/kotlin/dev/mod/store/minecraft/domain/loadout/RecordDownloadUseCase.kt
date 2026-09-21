package dev.mod.store.minecraft.domain.loadout

import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.domain.creation.CreationRepository

/** Which mods this device has already counted a download for. Persisted locally. */
interface DownloadLedger {
    fun isCounted(modId: Int): Boolean
    fun markCounted(modId: Int)
    fun unmark(modId: Int)
}

/**
 * Tells the backend a mod was downloaded — once per mod per device, and only after a file has
 * actually finished saving, so cancelled and stalled downloads never reach the statistics.
 *
 * A mod often ships several files, and a reader who takes two of them has still downloaded one
 * mod. The ledger is claimed *before* the request goes out: two files finishing back to back both
 * reach here, and the second sees the claim and stops. A failed request releases the claim, so the
 * next successful download gets another chance to be counted.
 *
 * Returns the server's new total, or null when nothing was sent because the mod was already
 * counted.
 */
class RecordDownloadUseCase(
    private val creationRepository: CreationRepository,
    private val ledger: DownloadLedger,
) {
    suspend operator fun invoke(modId: Int): Outcome<Int>? {
        if (ledger.isCounted(modId)) return null
        ledger.markCounted(modId)
        return creationRepository.recordDownload(modId).also { outcome ->
            if (outcome is Outcome.Failed) ledger.unmark(modId)
        }
    }
}
