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
        /**
         * The order these are offered to a reader: add-ons first because they are what most people
         * come for, then maps, textures and skins. Declaration order is a different thing and must
         * not be used for this — it exists for the wire format, not for the shelf.
         */
        val browseOrder: List<CreationCategory> = listOf(Addon, Maps, Texture, Skin)

        /**
         * Resolves the category reported inside a creation payload, defaulting to [Addon].
         *
         * The backend is not consistent about which spelling it sends: a creation reports `WORLD`
         * or `SKIN_PACK` — the same words the catalog filter takes — while other payloads use the
         * plain name. Matching both is why every map used to arrive labelled "Addon": `WORLD`
         * matches no enum name, and the fallback swallowed it.
         */
        fun fromResponse(value: String): CreationCategory =
            entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                    it.filterValue.equals(value, ignoreCase = true)
            } ?: Addon
    }
}
