package dev.mod.store.minecraft.domain.creation

/**
 * Everything the home screen shows above its browsing mode, gathered in one shot.
 *
 * Every list is allowed to be empty and [pickOfDay] to be null: the sections are loaded in
 * parallel and a single failing slice must not blank out the whole screen.
 */
data class HomeDigest(
    val pickOfDay: CreationEntity? = null,
    val trending: List<CreationEntity> = emptyList(),
    val popular: List<CreationEntity> = emptyList(),
    val topRated: List<CreationEntity> = emptyList(),
    val fresh: List<CreationEntity> = emptyList(),
    val catalogTotal: Int = 0,
) {
    /** True when not a single section came back with content — the screen has nothing to show. */
    val isEmpty: Boolean
        get() = pickOfDay == null &&
            trending.isEmpty() &&
            popular.isEmpty() &&
            topRated.isEmpty() &&
            fresh.isEmpty()

    /** Every creation on the screen, de-duplicated — used for the "supported versions" strip. */
    val everything: List<CreationEntity>
        get() = (listOfNotNull(pickOfDay) + trending + popular + topRated + fresh)
            .distinctBy { it.id }
}
