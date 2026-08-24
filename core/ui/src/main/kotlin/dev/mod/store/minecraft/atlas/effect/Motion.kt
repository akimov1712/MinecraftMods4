package dev.mod.store.minecraft.core.ui.effect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Tap handling with physical feedback: the surface dips under the finger and springs back,
 * instead of flashing a ripple. Used for every card-sized target in the app.
 */
@Composable
fun Modifier.tappable(
    enabled: Boolean = true,
    pressedScale: Float = 0.965f,
    onClick: () -> Unit,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press-scale",
    )
    return this
        .scale(scale)
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}

/**
 * Entrance choreography: content slides up and fades in, each [index] a beat later than the one
 * before it, so a screen assembles itself rather than appearing all at once.
 */
@Composable
fun Appear(
    index: Int = 0,
    modifier: Modifier = Modifier,
    stepMillis: Int = 55,
    durationMillis: Int = 420,
    travel: Dp = 24.dp,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = index * stepMillis,
                easing = EaseOutCubic,
            ),
        )
    }
    val travelPx = with(androidx.compose.ui.platform.LocalDensity.current) { travel.toPx() }
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * travelPx
        },
    ) {
        content()
    }
}

/**
 * A light sweep that crosses the surface every few seconds — the highlight that makes a hero
 * card look lit rather than printed.
 */
@Composable
fun Modifier.shine(
    periodMillis: Int = 4200,
    strength: Float = 0.16f,
): Modifier {
    val transition = rememberInfiniteTransition(label = "shine")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shine-progress",
    )
    return drawWithContent {
        drawContent()
        val span = size.width * 0.6f
        val start = -span + progress * (size.width + 2 * span)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = strength), Color.Transparent),
                start = Offset(start, 0f),
                end = Offset(start + span, size.height),
            ),
        )
    }
}

/** Slow breathing between two alphas — for glows that should feel alive but never blink. */
@Composable
fun pulse(from: Float = 0.35f, to: Float = 0.9f, periodMillis: Int = 2600): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val value by transition.animateFloat(
        initialValue = from,
        targetValue = to,
        animationSpec = infiniteRepeatable(tween(periodMillis), RepeatMode.Reverse),
        label = "pulse-value",
    )
    return value
}

/** Counts up to [value] instead of snapping to it, for the numbers on the stat tiles. */
@Composable
fun animatedCount(value: Int, durationMillis: Int = 900): Int {
    val progress by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis, easing = EaseOutCubic),
        label = "count",
    )
    return progress.roundToInt()
}
