package dev.mod.store.minecraft.feature.hub

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The tabbed destinations. Suggesting a mod is no longer one of them — it now lives on the mod
 * page — and search is an action in the bar rather than a tab of its own.
 */
enum class HubTab(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Showcase(R.string.hub_tab_showcase, Icons.Rounded.Home),
    Stash(R.string.hub_tab_stash, Icons.Rounded.BookmarkBorder),
    Compendium(R.string.hub_tab_compendium, Icons.AutoMirrored.Rounded.HelpOutline),
}
