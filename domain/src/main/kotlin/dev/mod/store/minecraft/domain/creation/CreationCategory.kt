package dev.mod.store.minecraft.domain.creation

/**
 * The kind of Minecraft content a creation delivers. Two string forms matter because the
 * backend is asymmetric: the catalog filter expects [filterValue], while a creation payload
 * reports its category by enum name (see [fromResponse]).
 */
enum class CreationCategory {
    Texture,
    Addon,
    Skin,
    Maps;

    /** Value the catalog `category` query parameter expects. */
    val filterValue: String
        get() = when (this) {
            Texture -> "TEXTURE_PACK"
            Addon -> "ADDON"
            Skin -> "SKIN_PACK"
            Maps -> "WORLD"
        }

    /** File extension a downloaded creation of this kind is installed with. */
    val fileExtension: String
        get() = when (this) {
            Texture -> ".mcpack"
            Skin -> ".mcpack"
            Maps -> ".mcworld"
            Addon -> ".mcaddon"
        }

    companion object {
        /** Resolves the category reported inside a creation payload, defaulting to [Addon]. */
        fun fromResponse(value: String): CreationCategory =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Addon
    }
}
