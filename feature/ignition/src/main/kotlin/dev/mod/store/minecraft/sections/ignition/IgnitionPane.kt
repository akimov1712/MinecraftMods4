package dev.mod.store.minecraft.feature.ignition

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.component.AppLogo
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.effect.CardShape
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.bob
import dev.mod.store.minecraft.core.ui.effect.halo
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.shine
import dev.mod.store.minecraft.core.ui.effect.wiggle
import dev.mod.store.minecraft.core.ui.effect.StoneBackdrop
import dev.mod.store.minecraft.core.ui.effect.card
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Intent
import dev.mod.store.minecraft.feature.ignition.IgnitionStore.Stage

/**
 * The opening screen, in two acts. First it loads, with a logo and one bar. Then it waits: a
 * single big button, and a promo beside it if one is ready. Nothing to read, nothing to decide.
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
        StoneBackdrop(heat = if (ready) 1.3f else 1f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            AppLogo(
                size = 96.dp,
                modifier = Modifier.then(if (ready) Modifier.bob(distance = 3.dp) else Modifier),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = appLabel(),
                color = Palette.TextPrimary,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ignition_tagline),
                color = Palette.TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            AnimatedContent(
                targetState = ready,
                transitionSpec = { fadeIn(tween(380)) togetherWith fadeOut(tween(180)) },
                modifier = Modifier.weight(1f),
                label = "ignition-stage",
            ) { isReady ->
                if (isReady) {
                    ReadyAct(
                        promoReady = state.promoReady,
                        onEnter = { component.onIntent(Intent.Enter) },
                    )
                } else {
                    LoadingAct(progress = progress)
                }
            }
        }
    }
}

@Composable
private fun LoadingAct(progress: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(CircleShape)
                .background(Palette.SurfaceHigh),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(Palette.Accent)
                    .shine(periodMillis = 1500, strength = 0.45f),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = Palette.Accent,
                modifier = Modifier
                    .size(18.dp)
                    .wiggle(degrees = 12f, periodMillis = 1800),
            )
            Text(
                text = stringResource(R.string.ignition_loading),
                color = Palette.TextMuted,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun ReadyAct(promoReady: Boolean, onEnter: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .popIn()
                .clip(CircleShape)
                .background(Palette.Positive.copy(alpha = 0.18f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = Palette.Positive,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = stringResource(R.string.ignition_ready_body),
                color = Palette.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        if (promoReady) {
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 200.dp, max = 420.dp)
                    .card(fill = Palette.Surface, shape = CardShape),
            ) {
                NativeSlot(slotKey = "ignition", modifier = Modifier.fillMaxSize())
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        PillButton(
            text = stringResource(R.string.ignition_enter),
            onClick = onEnter,
            modifier = Modifier
                .fillMaxWidth()
                .halo(Palette.Accent, SmallShape, radius = 20.dp, alpha = 0.5f)
                .heightIn(min = 64.dp),
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

@Composable
private fun appLabel(): String {
    val context = LocalContext.current
    return remember(context) {
        context.applicationInfo.loadLabel(context.packageManager).toString()
    }
}
