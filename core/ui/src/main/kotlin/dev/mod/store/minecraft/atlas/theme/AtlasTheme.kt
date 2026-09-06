package dev.mod.store.minecraft.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AtlasColorScheme = darkColorScheme(
    primary = Palette.Accent,
    onPrimary = Palette.OnAccentDark,
    secondary = Palette.Sky,
    onSecondary = Palette.OnAccentDark,
    tertiary = Palette.Ember,
    background = Palette.Canvas,
    onBackground = Palette.TextPrimary,
    surface = Palette.Surface,
    onSurface = Palette.TextPrimary,
    surfaceVariant = Palette.SurfaceHigh,
    outline = Palette.Stroke,
    error = Palette.Negative,
    onError = Palette.OnAccentDark,
)

/** Soft, friendly corners everywhere. */
private val AtlasShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(11.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

/** Wraps content in the app's dark colour scheme, soft shapes and type scale. */
@Composable
fun AtlasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AtlasColorScheme,
        shapes = AtlasShapes,
        typography = AtlasTypography,
        content = content,
    )
}
