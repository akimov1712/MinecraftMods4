package dev.mod.store.minecraft.feature.outreach

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.feature.outreach.OutreachStore.Intent

private val GUTTER = 20.dp
private val BLOCK_GAP = 14.dp
private val BLOCK_SHAPE = RoundedCornerShape(20.dp)
private val TILE_SHAPE = RoundedCornerShape(16.dp)

/** What a message can be about, and how each one is dressed. */
private data class Purpose(
    val mode: OutreachMode,
    val icon: ImageVector,
    val accent: Color,
    val titleRes: Int,
    val bodyRes: Int,
    val hintRes: Int,
)

private val PURPOSES = listOf(
    Purpose(
        mode = OutreachMode.Recommendation,
        icon = Icons.Rounded.Lightbulb,
        accent = Palette.Violet,
        titleRes = R.string.outreach_mode_recommend,
        bodyRes = R.string.outreach_mode_recommend_body,
        hintRes = R.string.outreach_message_recommend_hint,
    ),
    Purpose(
        mode = OutreachMode.Report,
        icon = Icons.Rounded.ReportProblem,
        accent = Palette.Ember,
        titleRes = R.string.outreach_mode_report,
        bodyRes = R.string.outreach_mode_report_body,
        hintRes = R.string.outreach_message_report_hint,
    ),
)

/**
 * Writing to us, laid out as a decision followed by a form rather than a stack of grey boxes.
 * First you say what the message is about — two cards, each explaining itself, and the one you pick
 * colours the rest of the screen. Then you write it, with the field labelled and counted so nothing
 * is a surprise. Sending swaps the whole page for a plain acknowledgement.
 */
@Composable
fun OutreachPane(
    component: OutreachComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val sentText = stringResource(R.string.outreach_sent)

    ObserveSignals(component.labels) { label ->
        when (label) {
            is OutreachStore.Label.Notify -> snackbar.showSnackbar(label.message)
            OutreachStore.Label.Sent -> snackbar.showSnackbar(sentText)
        }
    }

    val purpose = PURPOSES.first { it.mode == state.mode }
    val accent by animateColorAsState(purpose.accent, label = "outreach-accent")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.Canvas),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TopBar(onBack = component::back)

            if (state.sent) {
                SentPage(
                    hasAd = component.hasNativeAd,
                    onCompose = { component.onIntent(Intent.Compose) },
                )
            } else {
                FormPage(
                    state = state,
                    purpose = purpose,
                    accent = accent,
                    hasAd = component.hasNativeAd,
                    onIntent = component::onIntent,
                )
            }
        }

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
            onClick = onBack,
            size = 42.dp,
        )
        Text(
            text = stringResource(R.string.outreach_title),
            color = Palette.TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// region form

@Composable
private fun FormPage(
    state: OutreachStore.State,
    purpose: Purpose,
    accent: Color,
    hasAd: Boolean,
    onIntent: (Intent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "intro") {
            Intro(accent = accent)
        }

        item(key = "purpose") {
            Spacer(Modifier.height(BLOCK_GAP))
            Label(stringResource(R.string.outreach_purpose))
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier.padding(horizontal = GUTTER),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PURPOSES.forEach { entry ->
                    PurposeCard(
                        purpose = entry,
                        selected = entry.mode == state.mode,
                        onClick = { onIntent(Intent.SelectMode(entry.mode)) },
                    )
                }
            }
        }

        item(key = "form") {
            Spacer(Modifier.height(BLOCK_GAP))
            Label(stringResource(R.string.outreach_form))
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = GUTTER)
                    .clip(BLOCK_SHAPE)
                    .background(Palette.Surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FieldLabel(stringResource(R.string.outreach_email_label))
                OutlineField(
                    value = state.email,
                    onValueChange = { onIntent(Intent.ChangeEmail(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = stringResource(R.string.outreach_email_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Text(
                    text = stringResource(R.string.outreach_email_help),
                    color = Palette.TextFaint,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FieldLabel(stringResource(purpose.titleRes))
                    }
                    Text(
                        text = stringResource(
                            R.string.outreach_counter,
                            state.message.length,
                            MAX_MESSAGE_LENGTH,
                        ),
                        color = if (state.message.length >= MAX_MESSAGE_LENGTH) {
                            Palette.Ember
                        } else {
                            Palette.TextFaint
                        },
                        fontSize = 13.sp,
                    )
                }
                OutlineField(
                    value = state.message,
                    onValueChange = { onIntent(Intent.ChangeMessage(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = stringResource(purpose.hintRes),
                    singleLine = false,
                    minLines = 4,
                )
            }
        }

        item(key = "send") {
            Spacer(Modifier.height(BLOCK_GAP))
            PillButton(
                text = stringResource(R.string.outreach_submit),
                onClick = { onIntent(Intent.Submit) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GUTTER),
                enabled = state.canSubmit,
                busy = state.sending,
                container = accent,
                leading = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = null,
                        tint = Palette.OnAccentDark,
                        modifier = Modifier.size(19.dp),
                    )
                },
            )
        }

        // Below the button, where it is out of the way of every field on the page.
        if (hasAd) {
            item(key = "ad") {
                Spacer(Modifier.height(20.dp))
                NativeSlot(
                    slotKey = "outreach_form",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GUTTER),
                )
            }
        }

        item(key = "bottom") { Spacer(Modifier.navigationBarsPadding()) }
    }
}

/** The opening line: who reads this and what it is for. */
@Composable
private fun Intro(accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER)
            .clip(BLOCK_SHAPE)
            .background(Palette.Surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MarkEmailRead,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                text = stringResource(R.string.outreach_intro_title),
                color = Palette.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.outreach_intro_body),
                color = Palette.TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

/** One of the two reasons to write, stated in full instead of hidden behind a one-word tab. */
@Composable
private fun PurposeCard(
    purpose: Purpose,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border by animateColorAsState(
        targetValue = if (selected) purpose.accent else Palette.Stroke,
        label = "purpose-border",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TILE_SHAPE)
            .background(if (selected) purpose.accent.copy(alpha = 0.10f) else Palette.Surface)
            .border(if (selected) 1.5.dp else 1.dp, border, TILE_SHAPE)
            .tappable(pressedScale = 0.98f, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(purpose.accent.copy(alpha = if (selected) 0.22f else 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = purpose.icon,
                contentDescription = null,
                tint = purpose.accent,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = stringResource(purpose.titleRes),
                color = Palette.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(purpose.bodyRes),
                color = Palette.TextFaint,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }

        Box(
            modifier = Modifier
                .size(23.dp)
                .clip(CircleShape)
                .background(if (selected) purpose.accent else Color.Transparent)
                .border(if (selected) 0.dp else 1.5.dp, Palette.Stroke, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Palette.Canvas,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

// endregion

// region sent

/**
 * After sending there is nothing left to do on this screen, which makes it the one honest place on
 * it for an ad: the message is away, the reader is finished, and the panel above says so first.
 */
@Composable
private fun SentPage(hasAd: Boolean, onCompose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = GUTTER),
        verticalArrangement = Arrangement.spacedBy(BLOCK_GAP),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BLOCK_SHAPE)
                .background(Palette.Surface)
                .padding(horizontal = 20.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Palette.Positive.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Palette.Positive,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(
                text = stringResource(R.string.outreach_sent_title),
                color = Palette.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.outreach_sent_body),
                color = Palette.TextMuted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )
            PillButton(
                text = stringResource(R.string.outreach_sent_again),
                onClick = onCompose,
                modifier = Modifier.fillMaxWidth(),
                container = Palette.SurfaceHigh,
                content = Palette.TextPrimary,
            )
        }

        if (hasAd) {
            NativeSlot(
                slotKey = "outreach_sent",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// endregion

/** A section name, sitting on the page background above the block it introduces. */
@Composable
private fun Label(text: String) {
    Text(
        text = text,
        color = Palette.TextFaint,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = GUTTER),
    )
}

/** The name of a single field, inside its block. */
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Palette.TextMuted,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
    )
}
