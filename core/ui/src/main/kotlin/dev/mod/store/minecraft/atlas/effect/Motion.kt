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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.sin

/** Tap handling with a springy dip — every card and button in the app uses it. */
@Composable
fun Modifier.tappable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
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

/** Content slides up and fades in, each [index] a beat after the one before it. */
@Composable
fun Appear(
    index: Int = 0,
    modifier: Modifier = Modifier,
    stepMillis: Int = 60,
    durationMillis: Int = 420,
    travel: Dp = 26.dp,
    content: @Composable () -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, delayMillis = index * stepMillis, easing = EaseOutCubic),
        )
    }
    val travelPx = with(LocalDensity.current) { travel.toPx() }
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * travelPx
        },
    ) {
        content()
    }
}

/** Pops in with a bounce — badges, medals, anything that should feel like it landed. */
@Composable
fun Modifier.popIn(delayMillis: Int = 0): Modifier {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) kotlinx.coroutines.delay(delayMillis.toLong())
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        )
    }
    return this.scale(scale.value)
}

/** A slow up-and-down float, for badges and icons that should look alive. */
@Composable
fun Modifier.bob(distance: Dp = 3.dp, periodMillis: Int = 2200): Modifier {
    val transition = rememberInfiniteTransition(label = "bob")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "bob-phase",
    )
    val offsetPx = with(LocalDensity.current) { distance.toPx() }
    return this.graphicsLayer { translationY = sin(phase) * offsetPx }
}

/** A small friendly tilt back and forth — used on section icons. */
@Composable
fun Modifier.wiggle(degrees: Float = 7f, periodMillis: Int = 2600): Modifier {
    val transition = rememberInfiniteTransition(label = "wiggle")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "wiggle-phase",
    )
    return this.graphicsLayer { rotationZ = sin(phase) * degrees }
}

/** A soft light sweeping across a picture, so cards do not look like flat prints. */
@Composable
fun Modifier.shine(
    periodMillis: Int = 5200,
    strength: Float = 0.14f,
): Modifier {
    val transition = rememberInfiniteTransition(label = "shine")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing), RepeatMode.Restart),
        label = "shine-progress",
    )
    return drawWithContent {
        drawContent()
        val span = size.width * 0.55f
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

/** Slow breathing between two values — for gentle glows. */
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

/** Counts up to [value] instead of snapping to it. */
@Composable
fun animatedCount(value: Int, durationMillis: Int = 900): Int {
    val progress by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis, easing = EaseOutCubic),
        label = "count",
    )
    return progress.roundToInt()
}
