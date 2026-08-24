package dev.mod.store.minecraft.domain.review

/**
 * Persists the counters that drive the in-app review prompt. Policy (which launch numbers
 * trigger a prompt) lives with the consumer in :billboard; this only stores the state.
 */
interface ReviewTracker {
    /** Increments the cold-launch counter and returns the new value. */
    fun recordLaunch(): Int

    /** Whether the review form has ever been shown on this install. */
    var promptAlreadyShown: Boolean
}
