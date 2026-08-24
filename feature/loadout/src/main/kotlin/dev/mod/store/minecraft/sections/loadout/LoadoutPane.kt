package dev.mod.store.minecraft.feature.loadout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.modifier.pressable
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileItem
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileStatus
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Intent
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Notice

@Composable
fun LoadoutPane(
    component: LoadoutComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }
    val downloadFailed = stringResource(R.string.loadout_download_failed)
    val openFailed = stringResource(R.string.loadout_open_failed)

    ObserveSignals(component.labels) { label ->
        when (label) {
            is LoadoutStore.Label.Notify -> snackbar.showSnackbar(
                when (label.notice) {
                    Notice.DownloadFailed -> downloadFailed
                    Notice.OpenFailed -> openFailed
                },
            )
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Palette.Canvas)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            TopBar(
                title = state.title.ifBlank { stringResource(R.string.loadout_title) },
                onBack = component::back,
            )

            if (state.showVpnHint) VpnHint()

            when {
                state.stage is ScreenStage.Failed && state.items.isEmpty() ->
                    ErrorState(
                        message = (state.stage as ScreenStage.Failed).message,
                        onRetry = { component.onIntent(Intent.Retry) },
                        modifier = Modifier.padding(20.dp),
                    )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = state.items, key = { it.url }) { item ->
                        FileRow(item = item, component = component)
                    }
                }
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
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .pressable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = Palette.TextPrimary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Palette.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun VpnHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.CategoryAmber.copy(alpha = 0.16f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Info, contentDescription = null, tint = Palette.CategoryAmber, modifier = Modifier.size(20.dp))
        Text(
            text = stringResource(R.string.loadout_vpn_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Palette.TextPrimary,
        )
    }
}

@Composable
private fun FileRow(item: FileItem, component: LoadoutComponent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Palette.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = statusText(item),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.TextMuted,
                )
            }
            FileAction(item = item, component = component)
        }

        (item.status as? FileStatus.Downloading)?.let { downloading ->
            LinearProgressIndicator(
                progress = { downloading.fraction },
                modifier = Modifier.fillMaxWidth(),
                color = Palette.Accent,
                trackColor = Palette.SurfaceHigh,
            )
        }
    }
}

@Composable
private fun FileAction(item: FileItem, component: LoadoutComponent) {
    when (item.status) {
        FileStatus.Idle -> PillButton(
            text = stringResource(R.string.loadout_download),
            onClick = { component.onIntent(Intent.StartDownload(item.url)) },
        )

        is FileStatus.Downloading -> PillButton(
            text = stringResource(R.string.loadout_cancel),
            onClick = { component.onIntent(Intent.CancelDownload(item.url)) },
            container = Palette.SurfaceHigh,
            content = Palette.TextPrimary,
        )

        FileStatus.Ready -> PillButton(
            text = stringResource(R.string.loadout_install),
            onClick = { component.onIntent(Intent.Install(item.url)) },
            container = Palette.Positive,
        )
    }
}

@Composable
private fun statusText(item: FileItem): String = when (val status = item.status) {
    FileStatus.Idle -> formatBytes(item.sizeBytes) ?: stringResource(R.string.loadout_unknown_size)
    is FileStatus.Downloading -> stringResource(R.string.loadout_downloading, status.percent)
    FileStatus.Ready -> stringResource(R.string.loadout_ready)
}

private fun formatBytes(bytes: Long?): String? = when {
    bytes == null -> null
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
