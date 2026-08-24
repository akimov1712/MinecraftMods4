package dev.mod.store.minecraft.core.ui.effect

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.theme.Palette
import kotlin.math.sin
import kotlin.random.Random

private const val DRIFT_MS = 7_000
private const val EMBER_SEED = 77

private class Ember(
    val x: Float,
    val startY: Float,
    val size: Float,
    val speed: Float,
    val sway: Float,
    val warm: Boolean,
)

/**
 * Sparks drifting up from the lava seam. Purely decorative, drawn on a single canvas, and sized
 * in fractions of the surface so it works on any screen.
 */
@Composable
fun EmberField(
    modifier: Modifier = Modifier,
    count: Int = 26,
) {
    val embers = remember(count) {
        val random = Random(EMBER_SEED)
        List(count) {
            Ember(
                x = random.nextFloat(),
                startY = random.nextFloat(),
                size = 1.2f + random.nextFloat() * 2.6f,
                speed = 0.35f + random.nextFloat() * 0.85f,
                sway = 0.004f + random.nextFloat() * 0.02f,
                warm = random.nextFloat() > 0.35f,
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "embers")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(DRIFT_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ember-time",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        embers.forEach { ember ->
            val progress = (ember.startY + time * ember.speed) % 1f
            val y = size.height * (1f - progress)
            val x = size.width * ember.x + sin(progress * 12f) * size.width * ember.sway
            val fade = (1f - progress).coerceIn(0f, 1f)
            drawCircle(
                color = (if (ember.warm) Palette.Accent else Palette.Gold).copy(alpha = 0.10f + 0.5f * fade * fade),
                radius = ember.size.dp.toPx(),
                center = Offset(x, y),
            )
        }
    }
}
