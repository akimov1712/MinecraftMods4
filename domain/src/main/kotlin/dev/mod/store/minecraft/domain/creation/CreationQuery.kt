package dev.mod.store.minecraft.domain.creation

/** Everything that parameterises one page of a catalog request. */
data class CreationQuery(
    val searchText: String = "",
    val category: CreationCategory? = null,
    val feed: CreationFeed = CreationFeed.Trending,
    val offset: Int = 0,
    val limit: Int = 12,
)
