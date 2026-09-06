package dev.mod.store.minecraft.core.ui.effect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.mod.store.minecraft.core.ui.theme.Palette

/**
 * The background is one flat colour — no gradient, no glow, no texture. Everything that should
 * catch the eye (cover art, the action colour) is drawn on top of it.
 */
@Composable
fun StoneBackdrop(
    modifier: Modifier = Modifier,
    heat: Float = 1f,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.Canvas),
    )
}
