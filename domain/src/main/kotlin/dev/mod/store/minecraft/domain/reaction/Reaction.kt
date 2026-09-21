package dev.mod.store.minecraft.domain.reaction

import dev.mod.store.minecraft.core.common.outcome.Outcome

/** The reactions a reader can leave on a mod. Names match the backend's wire values exactly. */
enum class ReactionType { LIKE, FIRE, LOVE, FUNNY, WOW }

/**
 * Where a mod stands: how many readers chose each reaction, and which one — if any — this reader
 * chose. A reader holds at most one reaction per mod.
 */
data class ReactionSummary(
    val selected: ReactionType? = null,
    val counts: Map<ReactionType, Int> = emptyMap(),
    val total: Int = 0,
) {
    fun count(type: ReactionType): Int = counts[type] ?: 0

    /**
     * The summary as it will be once this reader's choice moves from [selected] to [next] —
     * worked out locally, so the screen can answer a tap before the server does. The old choice
     * gives its vote back and the new one takes it; the total moves only when a reaction is
     * added or removed outright.
     */
    fun choose(next: ReactionType?): ReactionSummary {
        if (next == selected) return this
        val updated = counts.toMutableMap()
        selected?.let { updated[it] = (updated[it] ?: 0).minus(1).coerceAtLeast(0) }
        next?.let { updated[it] = (updated[it] ?: 0) + 1 }
        val delta = (if (next != null) 1 else 0) - (if (selected != null) 1 else 0)
        return copy(selected = next, counts = updated, total = (total + delta).coerceAtLeast(0))
    }
}

/** Remote reactions. Implemented in :wire; every write carries this device's stable identity. */
interface ReactionRepository {
    suspend fun fetch(modId: Int): Outcome<ReactionSummary>
    suspend fun set(modId: Int, reaction: ReactionType?): Outcome<Unit>
}

/**
 * This device's memory of what it reacted with, and when it last wrote. Persisted locally, so a
 * choice survives the screen closing and shows instantly on reopen, and so writes can be spaced
 * out regardless of how fast a finger taps.
 */
interface ReactionLedger {
    fun selected(modId: Int): ReactionType?
    fun remember(modId: Int, reaction: ReactionType?)
    fun lastWriteAt(modId: Int): Long
    fun markWrite(modId: Int, atMillis: Long)
}
