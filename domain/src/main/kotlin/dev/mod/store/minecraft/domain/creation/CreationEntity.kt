package dev.mod.store.minecraft.domain.creation

/**
 * A single browsable/installable creation. [isBookmarked] is not part of the network payload —
 * it is merged in by the use case that combines the catalog with the local bookmark store.
 *
 * [rating], [commentCount], [reactionCount] and [publishedAtEpochMs] are the "social" numbers the
 * home screen decorates its cards with; they are absent from older payloads, hence the defaults.
 *
 * [trendingPosition], [similar] and [downloadsCount] only come with the single-mod request:
 * [trendingPosition] is this mod's 1-based place in the trending selection, or null when it is not
 * in it; [similar] is a handful of other mods from the same app, never this one.
 */
data class CreationEntity(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: CreationCategory,
    val gallery: List<String>,
    val fileUrls: List<String>,
    val supportedVersions: List<String>,
    val rating: Double = 0.0,
    val commentCount: Int = 0,
    val reactionCount: Int = 0,
    val publishedAtEpochMs: Long? = null,
    val isBookmarked: Boolean = false,
    val trendingPosition: Int? = null,
    val downloadsCount: Int = 0,
    val similar: List<CreationEntity> = emptyList(),
)
