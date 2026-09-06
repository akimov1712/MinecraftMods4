package dev.mod.store.minecraft.feature.loadout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileItem
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileStatus
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Intent
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Notice

private val SIDE_PADDING = 16.dp

/**
 * The download queue. Each file is a single strip that fills with colour as it arrives — the row
 * itself is the progress bar — and carries one round key that changes meaning as the file moves
 * from "not here" to "downloading" to "ready to open".
 */
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Header(
                title = state.title.ifBlank { stringResource(R.string.loadout_title) },
                ready = state.items.count { it.status is FileStatus.Ready },
                total = state.items.size,
                onBack = component::back,
            )

            when {
                state.stage is ScreenStage.Failed && state.items.isEmpty() ->
                    ErrorState(
                        message = (state.stage as ScreenStage.Failed).message,
                        onRetry = { component.onIntent(Intent.Retry) },
                        modifier = Modifier.padding(SIDE_PADDING),
                    )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = SIDE_PADDING, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items = state.items, key = { it.url }) { item ->
                        FileStrip(item = item, component = component)
                    }
                    if (state.showVpnHint) {
                        item(key = "vpn") { StalledNote() }
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
private fun Header(title: String, ready: Int, total: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
            onClick = onBack,
            size = 38.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Palette.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (total > 0) {
                Text(
                    text = stringResource(R.string.loadout_progress_summary, ready, total),
                    color = Palette.TextFaint,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

/** One file: the strip fills as it downloads, and the key on the right changes with its state. */
@Composable
private fun FileStrip(item: FileItem, component: LoadoutComponent) {
    val downloading = item.status as? FileStatus.Downloading
    val target = when {
        item.status is FileStatus.Ready -> 1f
        downloading != null -> downloading.fraction
        else -> 0f
    }
    val fill by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(280),
        label = "file-fill",
    )
    val fillColor = if (item.status is FileStatus.Ready) Palette.Positive else Palette.Accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(SmallShape)
            .background(Palette.Surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fill)
                .fillMaxHeight()
                .background(fillColor.copy(alpha = if (item.status is FileStatus.Ready) 0.12f else 0.22f)),
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = item.name,
                    color = Palette.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = statusText(item),
                    color = if (item.status is FileStatus.Ready) Palette.Positive else Palette.TextFaint,
                    fontSize = 12.sp,
                )
            }

            FileKey(item = item, component = component)
        }
    }
}

@Composable
private fun FileKey(item: FileItem, component: LoadoutComponent) {
    val (icon, tint, container, onClick) = when (item.status) {
        FileStatus.Idle -> Quad(
            Icons.Rounded.Download,
            Palette.OnAccentDark,
            Palette.Accent,
        ) { component.onIntent(Intent.StartDownload(item.url)) }

        is FileStatus.Downloading -> Quad(
            Icons.Rounded.Close,
            Palette.TextPrimary,
            Palette.SurfaceHigh,
        ) { component.onIntent(Intent.CancelDownload(item.url)) }

        FileStatus.Ready -> Quad(
            Icons.Rounded.PlayArrow,
            Palette.OnAccentDark,
            Palette.Positive,
        ) { component.onIntent(Intent.Install(item.url)) }
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(container)
            .tappable(onClick = onClick)
            .then(if (item.status is FileStatus.Ready) Modifier.popIn() else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Small carrier so the key's four properties can be destructured in one `when`. */
private data class Quad(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
    val container: Color,
    val onClick: () -> Unit,
)

@Composable
private fun StalledNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Bolt,
            contentDescription = null,
            tint = Palette.Gold,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.loadout_vpn_hint),
            color = Palette.TextMuted,
            fontSize = 12.sp,
            lineHeight = 17.sp,
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
