package dev.mod.store.minecraft.domain.review

/**
 * Persists the one fact behind the in-app review prompt. When to ask is policy, and it lives with
 * the consumer in :billboard; this only remembers whether it has happened.
 */
interface ReviewTracker {
    /** Whether the review form has ever been shown on this install. */
    var promptAlreadyShown: Boolean
}
