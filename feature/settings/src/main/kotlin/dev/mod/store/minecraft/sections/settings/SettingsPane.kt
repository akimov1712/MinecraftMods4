package dev.mod.store.minecraft.feature.settings

import android.content.Intent as AndroidIntent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.AppLogo
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.effect.CardShape
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.core.ui.util.storeLink
import dev.mod.store.minecraft.feature.settings.SettingsStore.Intent

private val SIDE_PADDING = 16.dp

/**
 * Settings, kept to things that actually do something: what the app is, what it has stored, and
 * the handful of places a reader may want to go from here.
 */
@Composable
fun SettingsPane(
    component: SettingsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val clearedText = stringResource(R.string.settings_cleared)

    ObserveSignals(component.labels) { label ->
        when (label) {
            SettingsStore.Label.Cleared -> snackbar.showSnackbar(clearedText)
        }
    }

    val appName = remember(context) {
        context.applicationInfo.loadLabel(context.packageManager).toString()
    }
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = SIDE_PADDING, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(R.string.settings_title),
                    color = Palette.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            item(key = "identity") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(Palette.Surface)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppLogo(size = 56.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = appName,
                            color = Palette.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.settings_version, version),
                            color = Palette.TextFaint,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            item(key = "saved") {
                SettingsGroup(title = stringResource(R.string.settings_group_data)) {
                    SettingsRow(
                        icon = Icons.Rounded.Bookmark,
                        title = stringResource(R.string.settings_saved),
                        value = state.savedCount.toString(),
                    )
                    SettingsRow(
                        icon = Icons.Rounded.DeleteSweep,
                        title = stringResource(R.string.settings_clear),
                        tint = Palette.Negative,
                        enabled = state.savedCount > 0,
                        onClick = { component.onIntent(Intent.AskClear) },
                    )
                }
            }

            item(key = "help") {
                SettingsGroup(title = stringResource(R.string.settings_group_help)) {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        title = stringResource(R.string.settings_walkthrough),
                        onClick = component::openWalkthrough,
                    )
                    SettingsRow(
                        icon = Icons.AutoMirrored.Rounded.Send,
                        title = stringResource(R.string.settings_write),
                        onClick = component::openOutreach,
                    )
                }
            }

            item(key = "app") {
                SettingsGroup(title = stringResource(R.string.settings_group_app)) {
                    SettingsRow(
                        icon = Icons.Rounded.StarRate,
                        title = stringResource(R.string.settings_rate),
                        onClick = {
                            val market = AndroidIntent(
                                AndroidIntent.ACTION_VIEW,
                                Uri.parse("market://details?id=${context.packageName}"),
                            ).addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(market) }
                        },
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Share,
                        title = stringResource(R.string.settings_share),
                        onClick = {
                            val share = AndroidIntent(AndroidIntent.ACTION_SEND).apply {
                                type = "text/plain"
                                // The link alone: a chat app renders its own preview, and the
                                // app's name in front of it just reads as spam.
                                putExtra(AndroidIntent.EXTRA_TEXT, storeLink(context))
                            }
                            runCatching {
                                context.startActivity(
                                    AndroidIntent.createChooser(share, null)
                                        .addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            }
                        },
                    )
                    SettingsRow(
                        icon = Icons.Rounded.Tune,
                        title = stringResource(R.string.settings_system),
                        onClick = {
                            val system = AndroidIntent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            ).addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(system) }
                        },
                    )
                }
            }

            // The very end of the list, under the last row a reader would actually press.
            if (component.hasNativeAd) {
                item(key = "ad") {
                    NativeSlot(slotKey = "settings", modifier = Modifier.fillMaxWidth())
                }
            }

            item(key = "bottom") { Spacer(Modifier.navigationBarsPadding()) }
        }

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }

    if (state.clearRequested) {
        ClearDialog(
            onConfirm = { component.onIntent(Intent.ConfirmClear) },
            onDismiss = { component.onIntent(Intent.DismissClear) },
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Palette.TextFaint,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(Palette.Surface),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    tint: Color = Palette.TextPrimary,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.tappable(enabled = enabled, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else Palette.TextFaint,
            modifier = Modifier.size(21.dp),
        )
        Text(
            text = title,
            color = if (enabled) tint else Palette.TextFaint,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                color = Palette.TextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ClearDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(color = Palette.SurfaceHigh, shape = SmallShape) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_clear_title),
                    color = Palette.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.settings_clear_body),
                    color = Palette.TextMuted,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillButton(
                        text = stringResource(R.string.settings_clear_cancel),
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        container = Palette.Surface,
                        content = Palette.TextPrimary,
                    )
                    PillButton(
                        text = stringResource(R.string.settings_clear_confirm),
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        container = Palette.Negative,
                        content = Palette.OnAccentDark,
                    )
                }
            }
        }
    }
}
