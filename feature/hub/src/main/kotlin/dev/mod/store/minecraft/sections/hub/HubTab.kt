package dev.mod.store.minecraft.feature.hub

import dev.mod.store.minecraft.core.ui.R
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Support
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The tabbed destinations. Icons are solid throughout — the bar reads as one set rather than a
 * mix of outlines and fills.
 */
enum class HubTab(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Showcase(R.string.hub_tab_showcase, Icons.Filled.Home),
    Stash(R.string.hub_tab_stash, Icons.Filled.Bookmark),
    Compendium(R.string.hub_tab_compendium, Icons.Filled.Support),
    Settings(R.string.hub_tab_settings, Icons.Filled.Settings),
}
