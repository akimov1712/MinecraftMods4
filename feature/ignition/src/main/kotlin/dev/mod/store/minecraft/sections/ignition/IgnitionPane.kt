package dev.mod.store.minecraft.feature.ignition

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.FullscreenNativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.AppLogo
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Intent
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Stage
import kotlin.math.roundToInt

/**
 * The splash is its own thing: square frames, corner brackets, a bracketed meter and monospace
 * figures, where the rest of the app is round. The one exception is the way out — that button is a
 * red pill like every other action in the app, because it is the handshake between the two, and it
 * floats over the page so a long promo scrolls beneath it rather than pushing it out of reach.
 */

private val MARGIN = 22.dp

private val BUTTON_HEIGHT = 58.dp

/** The breathing room the reader asked for between the end of the page and the button. */
private val BUTTON_GAP = 20.dp

/** What the floating button occupies, so the scrolling page can clear it. */
private val BUTTON_AREA = BUTTON_HEIGHT + BUTTON_GAP + 24.dp

/** Cells in the loading meter. Countable at a glance, and each one is a visible step. */
private const val METER_CELLS = 24

/** The lines that print under the meter while stage one works. */
private val HINTS = listOf(
    R.string.ignition_hint_free,
    R.string.ignition_hint_install,
    R.string.ignition_hint_save,
    R.string.ignition_hint_vpn,
    R.string.ignition_hint_search,
)

private val Mono = FontFamily.Monospace

@Composable
fun IgnitionPane(
    component: IgnitionComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val ready = state.stage == Stage.Ready
    val progress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(320, easing = EaseOutCubic),
        label = "boot-progress",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.Canvas),
    ) {
        Grid()

        // The promo is given a share of the screen rather than the leftovers, and the page scrolls
        // when the sum does not fit. On a tall screen nothing scrolls; on a short one everything
        // still reaches the button.
        val promoHeight = (maxHeight * 0.46f).coerceIn(240.dp, 420.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = MARGIN)
                .padding(top = MARGIN, bottom = if (ready) BUTTON_AREA else MARGIN),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(30.dp))

            Emblem()

            Spacer(Modifier.height(26.dp))

            Text(
                text = appLabel().uppercase(),
                color = Palette.TextPrimary,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Rule()

            Spacer(Modifier.height(30.dp))

            AnimatedContent(
                targetState = ready,
                transitionSpec = { fadeIn(tween(360)) togetherWith fadeOut(tween(160)) },
                label = "ignition-stage",
            ) { isReady ->
                if (isReady) {
                    Promo(promoReady = state.promoReady, height = promoHeight)
                } else {
                    LoadingStage(progress = progress, hint = state.hint)
                }
            }
        }

        if (ready) {
            EnterButton(
                onClick = { component.onIntent(Intent.Enter) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

// region chrome

/** Graph paper: thin rules, no blocks, no glow. Just enough to say the surface is not empty. */
@Composable
private fun Grid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 26.dp.toPx()
        val line = Palette.Stroke.copy(alpha = 0.35f)
        val width = 1.dp.toPx()

        var x = 0f
        while (x <= size.width) {
            drawLine(line, Offset(x, 0f), Offset(x, size.height), width)
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(line, Offset(0f, y), Offset(size.width, y), width)
            y += step
        }
    }
}

/** The launcher icon in a square frame, with accent brackets at the corners like a viewfinder. */
@Composable
private fun Emblem() {
    val side = 104.dp
    val frame = side + 24.dp

    Box(contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(frame).border(1.dp, Palette.Stroke))

        AppLogo(size = side, shape = RectangleShape)

        Canvas(modifier = Modifier.size(frame)) {
            val arm = 14.dp.toPx()
            val weight = 3.dp.toPx()
            val half = weight / 2f
            val w = size.width
            val h = size.height

            // Two arms per corner, drawn inside the frame so the strokes never clip.
            listOf(
                Offset(half, half) to (1f to 1f),
                Offset(w - half, half) to (-1f to 1f),
                Offset(half, h - half) to (1f to -1f),
                Offset(w - half, h - half) to (-1f to -1f),
            ).forEach { (corner, direction) ->
                val (dx, dy) = direction
                drawLine(
                    color = Palette.Accent,
                    start = corner,
                    end = Offset(corner.x + arm * dx, corner.y),
                    strokeWidth = weight,
                )
                drawLine(
                    color = Palette.Accent,
                    start = corner,
                    end = Offset(corner.x, corner.y + arm * dy),
                    strokeWidth = weight,
                )
            }
        }
    }
}

/** A hairline the width of the content, used instead of spacing to separate the title block. */
@Composable
private fun Rule() {
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(2.dp)
            .background(Palette.Accent),
    )
}

// endregion

// region stage one

@Composable
private fun LoadingStage(progress: Float, hint: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Meter(progress = progress)
        Console(hint = hint)
    }
}

/**
 * The wait as a bracketed meter: `[▓▓▓▓░░░░░░]` in square cells, with the figure spelled out in
 * monospace beside it so the two never disagree.
 */
@Composable
private fun Meter(progress: Float) {
    val clamped = progress.coerceIn(0f, 1f)
    val lit = (clamped * METER_CELLS)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Bracket(opening = true)

        Row(
            modifier = Modifier
                .weight(1f)
                .height(20.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            repeat(METER_CELLS) { index ->
                val fill = (lit - index).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (fill == 0f) {
                                Palette.SurfaceHigh
                            } else {
                                Palette.Accent.copy(alpha = 0.4f + 0.6f * fill)
                            },
                        ),
                )
            }
        }

        Bracket(opening = false)

        Text(
            text = stringResource(R.string.ignition_percent, (clamped * 100).roundToInt()),
            color = Palette.TextPrimary,
            fontFamily = Mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** One square bracket of the meter, drawn as three bars rather than a glyph so it always lines up. */
@Composable
private fun Bracket(opening: Boolean) {
    Column(
        modifier = Modifier.height(20.dp),
        horizontalAlignment = if (opening) Alignment.Start else Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(Modifier.width(6.dp).height(2.dp).background(Palette.TextFaint))
        Box(Modifier.width(2.dp).height(12.dp).background(Palette.TextFaint))
        Box(Modifier.width(6.dp).height(2.dp).background(Palette.TextFaint))
    }
}

/** A tip, printed like a console line, with a cursor blinking after it. */
@Composable
private fun Console(hint: Int) {
    val blink by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "cursor-blink",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "> ",
            color = Palette.Accent,
            fontFamily = Mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        AnimatedContent(
            targetState = hint % HINTS.size,
            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(140)) },
            label = "ignition-hint",
        ) { index ->
            Text(
                text = stringResource(HINTS[index]),
                color = Palette.TextMuted,
                fontFamily = Mono,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
        }
        Text(
            text = "_",
            color = if (blink < 1f) Palette.Accent else Color.Transparent,
            fontFamily = Mono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// endregion

// region stage two

/**
 * The promo, at a definite height because the page around it scrolls and a weighted child cannot
 * be measured inside a scroll. The filling ad form stretches its artwork to whatever it is given,
 * so the box is always full.
 */
@Composable
private fun Promo(promoReady: Boolean, height: androidx.compose.ui.unit.Dp) {
    if (!promoReady) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, Palette.Stroke)
            .padding(1.dp),
    ) {
        FullscreenNativeSlot(
            slotKey = "ignition",
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * The way into the app, in the app's own language rather than the splash's: a red pill, the shape
 * of every other action the reader is about to meet. It floats over the page, clear of the system
 * bar, so a long promo scrolls beneath it instead of pushing it out of reach.
 */
@Composable
private fun EnterButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.45f to Palette.Canvas.copy(alpha = 0.85f),
                    1f to Palette.Canvas,
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = MARGIN)
            .padding(top = 24.dp, bottom = BUTTON_GAP),
    ) {
        PillButton(
            text = stringResource(R.string.ignition_enter),
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(BUTTON_HEIGHT),
            leading = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Palette.OnAccentDark,
                    modifier = Modifier.size(22.dp),
                )
            },
        )
    }
}

// endregion

@Composable
private fun appLabel(): String {
    val context = LocalContext.current
    return remember(context) {
        context.applicationInfo.loadLabel(context.packageManager).toString()
    }
}
