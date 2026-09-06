package dev.mod.store.minecraft.core.ui.effect

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import dev.mod.store.minecraft.core.ui.theme.Palette

/**
 * The background: one calm colour with a warm glow behind the top of the screen. No moving
 * lights, no texture — the covers of the mods should be the only busy thing on screen.
 */
@Composable
fun StoneBackdrop(
    modifier: Modifier = Modifier,
    heat: Float = 1f,
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.Canvas),
    ) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Palette.Accent.copy(alpha = 0.10f * heat),
                    Palette.Canvas.copy(alpha = 0f),
                ),
                center = Offset(size.width / 2f, size.height * 0.08f),
                radius = size.width * 1.1f,
            ),
        )
    }
}
