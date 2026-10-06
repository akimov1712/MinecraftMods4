package dev.mod.store.minecraft.feature.hub

import dev.mod.store.minecraft.core.ui.R
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The four places you can be. The glyphs are objects rather than symbols — a compass to find your
 * way, a chest for what you kept, a lamp for an answer, tools for everything else — which is how
 * the game itself labels things.
 */
enum class HubTab(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Showcase(R.string.hub_tab_showcase, Icons.Rounded.Explore),
    Stash(R.string.hub_tab_stash, Icons.Rounded.Inventory2),
    Compendium(R.string.hub_tab_compendium, Icons.Rounded.Lightbulb),
    Settings(R.string.hub_tab_settings, Icons.Rounded.Handyman),
}
