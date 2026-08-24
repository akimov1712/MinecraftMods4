package dev.mod.store.minecraft.core.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import dev.mod.store.minecraft.core.ui.theme.Palette

private const val SWEEP_WIDTH = 600f

/** A self-animating gradient sweep used as the shimmer placeholder fill. */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-progress",
    )
    return remember(progress) {
        val start = -SWEEP_WIDTH + progress * (2 * SWEEP_WIDTH)
        Brush.linearGradient(
            colors = listOf(Palette.ShimmerBase, Palette.ShimmerHighlight, Palette.ShimmerBase),
            start = Offset(start, 0f),
            end = Offset(start + SWEEP_WIDTH, 0f),
        )
    }
}

/** Paints [this] with the animated shimmer sweep, optionally clipped to [shape]. */
@Composable
fun Modifier.shimmerSheen(shape: Shape = RectangleShape): Modifier =
    this.clip(shape).background(rememberShimmerBrush())

/** A standalone shimmering block — the building brick for content skeletons. */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
) {
    Box(modifier = modifier.shimmerSheen(shape))
}
