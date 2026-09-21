package dev.mod.store.minecraft.domain.reaction

import dev.mod.store.minecraft.core.common.error.AppError
import dev.mod.store.minecraft.core.common.outcome.Outcome

/** Shortest gap between two reaction writes for the same mod from this device. */
private const val WRITE_COOLDOWN_MS = 1_200L

/**
 * Loads a mod's reactions. The server is the authority on the counts and, since it keys choices by
 * this device's stable identity, on which reaction is selected too — so a successful load also
 * refreshes the local memory. When the network fails, the local choice is still returned as a
 * fallback, so the reader's own reaction never disappears just because they are offline.
 */
class FetchReactionsUseCase(
    private val repository: ReactionRepository,
    private val ledger: ReactionLedger,
) {
    suspend operator fun invoke(modId: Int): Outcome<ReactionSummary> =
        when (val outcome = repository.fetch(modId)) {
            is Outcome.Done -> {
                ledger.remember(modId, outcome.value.selected)
                outcome
            }
            is Outcome.Failed -> Outcome.Failed(
                error = outcome.error,
                value = ReactionSummary(selected = ledger.selected(modId)),
            )
        }
}

/** What happened to a reaction write. */
sealed interface ReactionWrite {
    /** The server accepted it. */
    data object Applied : ReactionWrite

    /** Refused locally: the previous write for this mod was too recent. Nothing was sent. */
    data object Throttled : ReactionWrite

    data class Failed(val error: AppError) : ReactionWrite
}

/**
 * Sets, changes or clears this reader's reaction.
 *
 * Inflating a mod's counts is not something a tap can do — the backend keeps exactly one reaction
 * per identity and each write replaces the last. What this guards against is the request flood a
 * restless finger produces: writes for one mod are spaced at least [WRITE_COOLDOWN_MS] apart, and
 * a write that would change nothing is never sent. The stamp is taken before the request goes out,
 * so a second tap arriving while the first is still in flight is refused too.
 */
class SetReactionUseCase(
    private val repository: ReactionRepository,
    private val ledger: ReactionLedger,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    suspend operator fun invoke(modId: Int, reaction: ReactionType?): ReactionWrite {
        val now = clock()
        if (now - ledger.lastWriteAt(modId) < WRITE_COOLDOWN_MS) return ReactionWrite.Throttled
        if (reaction == ledger.selected(modId)) return ReactionWrite.Applied

        ledger.markWrite(modId, now)
        return when (val outcome = repository.set(modId, reaction)) {
            is Outcome.Done -> {
                ledger.remember(modId, reaction)
                ReactionWrite.Applied
            }
            is Outcome.Failed -> ReactionWrite.Failed(outcome.error)
        }
    }
}
