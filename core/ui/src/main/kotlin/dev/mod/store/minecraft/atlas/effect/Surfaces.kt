package dev.mod.store.minecraft.core.ui.effect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.theme.Palette

/** Coloured halo under a surface — how accents "leak" light onto the canvas around them. */
fun Modifier.halo(
    color: Color,
    shape: Shape,
    radius: Dp = 24.dp,
    alpha: Float = 0.6f,
): Modifier = shadow(
    elevation = radius,
    shape = shape,
    clip = false,
    ambientColor = color.copy(alpha = alpha),
    spotColor = color.copy(alpha = alpha),
)

/**
 * The standard panel: translucent fill, hairline edge, optional accent tint. Everything that is
 * not artwork sits on one of these.
 */
fun Modifier.panel(
    shape: Shape,
    tint: Color = Palette.Surface,
    stroke: Color = Palette.GlassStroke,
    strokeWidth: Dp = 1.dp,
): Modifier = this
    .clip(shape)
    .background(tint)
    .border(strokeWidth, stroke, shape)

/** Panel whose edge is a gradient — reserved for hero surfaces and primary actions. */
fun Modifier.gradientPanel(
    shape: Shape,
    fill: Brush,
    edge: Brush,
    strokeWidth: Dp = 1.5.dp,
): Modifier = this
    .clip(shape)
    .background(fill)
    .border(strokeWidth, edge, shape)
