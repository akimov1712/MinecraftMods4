package dev.mod.store.minecraft.domain.creation

/**
 * The curated slices of the catalog the home screen is built from. Each one is a different
 * ordering (or a different endpoint) of the same pool of creations — the mapping to concrete
 * backend paths and sort keys lives in :data:network, not here.
 */
enum class CreationFeed {
    /** Editorial order — what the backend considers most relevant right now. */
    Trending,

    /** Most installed first. */
    Popular,

    /** Highest rated first. */
    TopRated,

    /** Most recently added first. */
    Fresh,
}
