package dev.mod.store.minecraft.domain.creation

/**
 * A single browsable/installable creation. [isBookmarked] is not part of the network payload —
 * it is merged in by the use case that combines the catalog with the local bookmark store.
 *
 * [rating], [commentCount], [reactionCount] and [publishedAtEpochMs] are the "social" numbers the
 * home screen decorates its cards with; they are absent from older payloads, hence the defaults.
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
)
