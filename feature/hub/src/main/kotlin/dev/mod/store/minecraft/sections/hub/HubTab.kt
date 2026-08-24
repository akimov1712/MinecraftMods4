package dev.mod.store.minecraft.feature.hub

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.ui.graphics.vector.ImageVector

/** The four bottom-bar destinations. Order here is the order they appear in the bar. */
enum class HubTab(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Showcase(R.string.hub_tab_showcase, Icons.Rounded.GridView),
    Stash(R.string.hub_tab_stash, Icons.Rounded.BookmarkBorder),
    Outreach(R.string.hub_tab_outreach, Icons.AutoMirrored.Rounded.Send),
    Compendium(R.string.hub_tab_compendium, Icons.AutoMirrored.Rounded.HelpOutline),
}
