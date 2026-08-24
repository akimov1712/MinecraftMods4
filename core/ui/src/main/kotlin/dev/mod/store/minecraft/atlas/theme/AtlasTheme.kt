package dev.mod.store.minecraft.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

/** Wraps content in the app's dark colour scheme and type scale. */
@Composable
fun AtlasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AtlasColorScheme,
        typography = AtlasTypography,
        content = content,
    )
}
