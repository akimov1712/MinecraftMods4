package dev.mod.store.minecraft.core.ui.effect

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.theme.Palette
import kotlin.math.sin
import kotlin.random.Random

private val BLOCK = 56.dp
private const val STONE_SEED = 20260820
private const val EMBER_CYCLE_MS = 9_000

/**
 * The wall the app is carved into: flat charcoal with an uneven stone lattice, a vignette that
 * darkens the corners, and the faintest heat rising from the bottom edge. Deliberately flat —
 * the colour on screen should come from cover art and lava-hot accents, not from the backdrop.
 */
@Composable
fun StoneBackdrop(
    modifier: Modifier = Modifier,
    heat: Float = 1f,
) {
    val transition = rememberInfiniteTransition(label = "stone")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(EMBER_CYCLE_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "stone-phase",
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.Canvas),
    ) {
        stoneLattice()
        heatSeam(phase, heat)
        vignette()
    }
}

/** Blocks of very slightly different darkness, laid out like a quarried wall. */
private fun DrawScope.stoneLattice() {
    val step = BLOCK.toPx()
    val columns = (size.width / step).toInt() + 2
    val rows = (size.height / step).toInt() + 2
    val random = Random(STONE_SEED)

    for (row in 0 until rows) {
        for (column in 0 until columns) {
            val shade = random.nextFloat()
            if (shade < 0.72f) continue
            val offset = if (row % 2 == 0) 0f else step / 2f
            drawRect(
                color = Color.White.copy(alpha = 0.006f + shade * 0.010f),
                topLeft = Offset(column * step - offset, row * step),
                size = Size(step - 1.5f, step - 1.5f),
            )
        }
    }
}

/** A lava seam breathing along the bottom edge — the only warmth in the backdrop. */
private fun DrawScope.heatSeam(phase: Float, heat: Float) {
    val intensity = (0.35f + 0.15f * sin(phase)) * heat
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Palette.AccentDeep.copy(alpha = 0.10f * intensity),
                Palette.Accent.copy(alpha = 0.16f * intensity),
            ),
            startY = size.height * 0.62f,
            endY = size.height,
        ),
    )
}

private fun DrawScope.vignette() {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
            center = Offset(size.width / 2f, size.height * 0.38f),
            radius = size.width * 1.15f,
        ),
    )
}
