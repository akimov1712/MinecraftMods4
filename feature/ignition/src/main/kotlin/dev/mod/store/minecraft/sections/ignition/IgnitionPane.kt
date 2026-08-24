package dev.mod.store.minecraft.feature.ignition

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.effect.EmberField
import dev.mod.store.minecraft.core.ui.effect.StoneBackdrop
import dev.mod.store.minecraft.core.ui.effect.halo
import dev.mod.store.minecraft.core.ui.effect.panel
import dev.mod.store.minecraft.core.ui.effect.pulse
import dev.mod.store.minecraft.core.ui.effect.shine
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Intent
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Stage

private val EMBLEM_SIZE = 188.dp

/**
 * The two-act opening, staged like a forge: sparks drift up from the lava seam while the emblem
 * heats block by block and a ring tracks the boot. Act two hands control to the user — a promo
 * and one button.
 */
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

    Box(modifier = modifier.fillMaxSize()) {
        StoneBackdrop(heat = if (ready) 1.5f else 0.9f)
        EmberField(count = if (ready) 34 else 22)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val topSpace by animateDpAsState(
                targetValue = if (ready) 8.dp else 72.dp,
                animationSpec = tween(500, easing = EaseOutCubic),
                label = "top-space",
            )
            Spacer(Modifier.height(topSpace))

            ForgeEmblem(progress = progress, ready = ready)

            Spacer(Modifier.height(22.dp))

            Text(
                text = appLabel(),
                color = Palette.TextPrimary,
                fontSize = 32.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.ignition_tagline),
                color = Palette.TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            AnimatedContent(
                targetState = ready,
                transitionSpec = {
                    (fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 4 })
                        .togetherWith(fadeOut(tween(200)))
                },
                modifier = Modifier.weight(1f),
                label = "ignition-stage",
            ) { isReady ->
                if (isReady) {
                    ReadyAct(
                        promoReady = state.promoReady,
                        onEnter = { component.onIntent(Intent.Enter) },
                    )
                } else {
                    LoadingAct(progress = progress, hint = state.hint)
                }
            }
        }
    }
}

// region act one

@Composable
private fun LoadingAct(progress: Float, hint: Int) {
    val hints = stringArrayResource(R.array.ignition_hints)
    val steps = stringArrayResource(R.array.ignition_steps)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedContent(
            targetState = hints[hint % hints.size],
            transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
            label = "ignition-hint",
        ) { line ->
            Text(
                text = line,
                color = Palette.TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(22.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .panel(RoundedCornerShape(24.dp), tint = Palette.Surface.copy(alpha = 0.7f))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            steps.forEachIndexed { index, step ->
                BootStep(
                    label = step,
                    done = progress >= (index + 1) / (steps.size + 0.4f),
                )
            }
        }
    }
}

/** One line of the boot checklist; ticks over as the bar passes its share of the progress. */
@Composable
private fun BootStep(label: String, done: Boolean) {
    val tint by animateColorAsState(
        targetValue = if (done) Palette.Accent else Palette.TextFaint,
        animationSpec = tween(280),
        label = "step-tint",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = if (done) 0.9f else 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            if (done) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Palette.OnAccentDark,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
        Text(
            text = label,
            color = if (done) Palette.TextPrimary else Palette.TextFaint,
            fontSize = 13.sp,
            fontWeight = if (done) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

// endregion

// region act two

@Composable
private fun ReadyAct(promoReady: Boolean, onEnter: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Palette.Accent.copy(alpha = 0.14f))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Palette.Accent),
            )
            Text(
                text = stringResource(R.string.ignition_ready_badge),
                color = Palette.AccentSoft,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.ignition_ready_body),
            color = Palette.TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )

        if (promoReady) {
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 200.dp, max = 420.dp)
                    .panel(RoundedCornerShape(28.dp), tint = Palette.Surface.copy(alpha = 0.8f)),
            ) {
                NativeSlot(slotKey = "ignition", modifier = Modifier.fillMaxSize())
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(18.dp))

        EnterButton(onClick = onEnter)
    }
}

@Composable
private fun EnterButton(onClick: () -> Unit) {
    val glow = pulse(from = 0.3f, to = 0.7f, periodMillis = 1800)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .halo(Palette.Accent, CircleShape, radius = 26.dp, alpha = glow)
            .clip(CircleShape)
            .background(Palette.AccentGradient)
            .tappable(onClick = onClick)
            .shine(periodMillis = 3200, strength = 0.20f)
            .padding(horizontal = 26.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.ignition_enter),
            color = Palette.OnAccentDark,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            tint = Palette.OnAccentDark,
            modifier = Modifier.size(22.dp),
        )
    }
}

// endregion

/**
 * Nine blocks heating up inside a progress ring: the loader, the percentage and the logotype in
 * one shape. When the boot finishes the whole thing glows and breathes.
 */
@Composable
private fun ForgeEmblem(progress: Float, ready: Boolean) {
    val lit = (progress * 9).toInt()
    val breath = pulse(from = 0.55f, to = 1f, periodMillis = 2200)
    val scale by animateFloatAsState(
        targetValue = if (ready) 1.05f else 1f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "emblem-scale",
    )

    Box(
        modifier = Modifier
            .size(EMBLEM_SIZE)
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        ProgressRing(progress = progress, ready = ready, breath = breath)

        Box(
            modifier = Modifier
                .halo(Palette.Accent, RoundedCornerShape(26.dp), radius = 30.dp, alpha = if (ready) breath else 0.35f)
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(Palette.SurfaceHigh, Palette.Surface)))
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(3) { column ->
                            val index = row * 3 + column
                            val active = ready || index < lit
                            val target = when {
                                !active -> Palette.Stroke
                                index % 3 == 0 -> Palette.Accent
                                index % 3 == 1 -> Palette.Ember
                                else -> Palette.Gold
                            }
                            val color by animateColorAsState(
                                targetValue = if (ready) target.copy(alpha = breath) else target,
                                animationSpec = tween(320),
                                label = "block-$index",
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color),
                            )
                        }
                    }
                }
            }
        }

        if (!ready) {
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Palette.OnAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(Palette.Canvas.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, ready: Boolean, breath: Float) {
    val sweep = 360f * progress.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 6.dp.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)

        drawArc(
            color = Palette.Stroke,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawArc(
            brush = Brush.sweepGradient(
                listOf(Palette.AccentDeep, Palette.Accent, Palette.Gold, Palette.Accent),
            ),
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            alpha = if (ready) breath else 1f,
        )
    }
}

@Composable
private fun appLabel(): String {
    val context = LocalContext.current
    return remember(context) {
        context.applicationInfo.loadLabel(context.packageManager).toString()
    }
}
