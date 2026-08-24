package dev.mod.store.minecraft.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Face
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.domain.creation.CreationCategory

/** Localized label for a creation category. */
@Composable
fun creationCategoryLabel(category: CreationCategory): String = stringResource(
    when (category) {
        CreationCategory.Addon -> R.string.category_addon
        CreationCategory.Maps -> R.string.category_maps
        CreationCategory.Texture -> R.string.category_texture
        CreationCategory.Skin -> R.string.category_skin
    },
)

/** Accent colour used to tint a category's pill. */
fun creationCategoryAccent(category: CreationCategory): Color = when (category) {
    CreationCategory.Addon -> Palette.CategoryLagoon
    CreationCategory.Maps -> Palette.CategoryLime
    CreationCategory.Texture -> Palette.CategoryRose
    CreationCategory.Skin -> Palette.CategoryAmber
}

/** Second colour of a category tile's gradient — the accent darkened towards the canvas. */
fun creationCategoryShade(category: CreationCategory): Color = when (category) {
    CreationCategory.Addon -> Color(0xFF0E7490)
    CreationCategory.Maps -> Color(0xFF4D7C0F)
    CreationCategory.Texture -> Color(0xFF9D174D)
    CreationCategory.Skin -> Color(0xFFB45309)
}

/** Glyph that stands in for a category wherever there is no artwork. */
fun creationCategoryIcon(category: CreationCategory): ImageVector = when (category) {
    CreationCategory.Addon -> Icons.Rounded.Extension
    CreationCategory.Maps -> Icons.Rounded.Map
    CreationCategory.Texture -> Icons.Rounded.Brush
    CreationCategory.Skin -> Icons.Rounded.Face
}
