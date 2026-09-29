package dev.mod.store.minecraft.domain.config

/**
 * One native ad slot the admin panel knows about.
 *
 * The config response carries an `ads` array pairing an [id] with an on/off flag, so a single slot
 * can be muted from the panel without shipping a build. This enum is the contract between that
 * array and the call sites: an id the app does not know is ignored, and a slot the server never
 * mentions stays switched on — a placement added here does not go dark while the backend is still
 * catching up.
 *
 * The ids are the panel's, not the app's, so they keep the backend's spelling (`addon_*`) even
 * where this app's own screens are named differently.
 */
enum class AdPlacement(val id: String, val fullscreen: Boolean = false) {

    /** Native block on the second stage of the splash. */
    LOADER_NATIVE("loader_native", fullscreen = true),

    /** The full-screen promo between the splash and the catalog. */
    ADDON_OPEN_FULLSCREEN_NATIVE("addon_open_fullscreen_native", fullscreen = true),

    /** Between the sections of the home feed. */
    HOME_LIST_NATIVE("home_list_native"),

    /** In an opened "see all" list — trending, popular, fresh, top rated. */
    CATALOG_LIST_NATIVE("catalog_list_native"),

    /** In the search screen, both the trending suggestions and the results. */
    SEARCH_LIST_NATIVE("search_list_native"),

    /** Under the description on the mod page. */
    ADDON_DETAILS_NATIVE("addon_details_native"),

    /** Above the similar mods on the mod page. */
    ADDON_RELATED_NATIVE("addon_related_native"),

    /** In the list of files to download. */
    DOWNLOADS_LIST_NATIVE("downloads_list_native"),

    /** In the bookmarks list. */
    FAVORITES_LIST_NATIVE("favorites_list_native"),

    /** In the install guide. */
    GUIDE_NATIVE("guide_native"),

    /** At the end of the FAQ conversation. */
    FAQ_NATIVE("faq_native"),

    /** On the contact screen, under the form and on the confirmation. */
    SUGGEST_NATIVE("suggest_native"),

    /** At the foot of the settings screen. */
    SETTINGS_NATIVE("settings_native");

    companion object {

        fun fromId(id: String?): AdPlacement? = entries.firstOrNull { it.id == id }
    }
}
