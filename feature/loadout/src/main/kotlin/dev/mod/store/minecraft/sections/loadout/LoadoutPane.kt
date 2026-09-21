package dev.mod.store.minecraft.feature.loadout

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileItem
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.FileStatus
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Intent
import dev.mod.store.minecraft.feature.loadout.LoadoutStore.Notice

private val GUTTER = 20.dp
private val GAP = 12.dp
private val CARD_SHAPE = RoundedCornerShape(18.dp)

/**
 * Getting the files, kept to the one thing that matters: for each file there is a button, and the
 * button says what it does right now — download it, stop, or open it in the game. Above the list,
 * permanently, sits the VPN note, because a blocked server is what goes wrong here and a reader who
 * has to work that out alone has usually given up first.
 */
@Composable
fun LoadoutPane(
    component: LoadoutComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
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
            TopBar(
                title = state.title.ifBlank { stringResource(R.string.loadout_title) },
                onBack = component::back,
            )

            when {
                state.stage is ScreenStage.Failed && state.items.isEmpty() ->
                    ErrorState(
                        message = (state.stage as ScreenStage.Failed).message,
                        onRetry = { component.onIntent(Intent.Retry) },
                        modifier = Modifier.padding(GUTTER),
                    )

                state.items.isEmpty() && state.stage.isLoading -> LoadingPage()

                state.items.isEmpty() -> EmptyPage()

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(GAP),
                ) {
                    // Always on screen: by the time someone works out that a download is not
                    // moving, they have usually already left. Saying it up front costs one card.
                    item(key = "vpn") { VpnNote(urgent = state.showVpnHint) }

                    items(items = state.items, key = { it.url }) { item ->
                        FileCard(item = item, component = component)
                    }

                    if (component.hasNativeAd) {
                        item(key = "ad") {
                            NativeSlot(
                                slotKey = "loadout_files",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = GUTTER),
                            )
                        }
                    }

                    item(key = "bottom") { Spacer(Modifier.navigationBarsPadding()) }
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
            text = title,
            color = Palette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * One file, on one line: a round badge that shows at a glance whether it is waiting, coming or
 * done, the name beside it, and a short button naming the single thing to do next. Neither the name
 * nor the status is allowed to wrap — a long file name scrolls past the way a track title does on a
 * music player — so the row keeps its height however wide the button gets. The progress bar exists
 * only while it is needed, tucked under the row rather than adding a third block.
 */
@Composable
private fun FileCard(item: FileItem, component: LoadoutComponent) {
    val downloading = item.status as? FileStatus.Downloading
    val ready = item.status is FileStatus.Ready
    val tint = when {
        ready -> Palette.Positive
        downloading != null -> Palette.Sky
        else -> Palette.Accent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER)
            .clip(CARD_SHAPE)
            .background(Palette.Surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when {
                        ready -> Icons.Rounded.Check
                        downloading != null -> Icons.Rounded.Downloading
                        else -> Icons.Rounded.Download
                    },
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                // A file name is long and a button is wide, so anything that wraps here turns the
                // card into four ragged lines. Nothing wraps: the name slides past instead.
                Text(
                    text = item.name,
                    color = Palette.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                )
                Text(
                    text = statusText(item),
                    color = if (ready) Palette.Positive else Palette.TextFaint,
                    fontSize = 13.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            when {
                ready -> ActionButton(
                    text = stringResource(R.string.loadout_action_open),
                    container = Palette.Positive,
                    content = Palette.Canvas,
                    onClick = { component.onIntent(Intent.Install(item.url)) },
                )

                downloading != null -> ActionButton(
                    text = stringResource(R.string.loadout_cancel),
                    container = Palette.SurfaceHigh,
                    content = Palette.TextPrimary,
                    onClick = { component.onIntent(Intent.CancelDownload(item.url)) },
                )

                else -> ActionButton(
                    text = stringResource(R.string.loadout_download),
                    container = Palette.Accent,
                    content = Palette.OnAccentDark,
                    onClick = { component.onIntent(Intent.StartDownload(item.url)) },
                )
            }
        }

        if (downloading != null) {
            ProgressLine(downloading)
        }
    }
}

/** The bar and its percentage, on one line, replacing the status text while a file is arriving. */
@Composable
private fun ProgressLine(status: FileStatus.Downloading) {
    val fill by animateFloatAsState(
        targetValue = status.fraction,
        animationSpec = tween(280),
        label = "file-fill",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape)
                .background(Palette.SurfaceHigh),
        ) {
            if (status.hasKnownTotal) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Palette.Accent),
                )
            } else {
                // The server gave no size, so sweep rather than invent a percentage.
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    shape = CircleShape,
                )
            }
        }
        if (status.hasKnownTotal) {
            Text(
                text = stringResource(R.string.loadout_percent, status.percent),
                color = Palette.TextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = content,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .clip(CircleShape)
            .background(container)
            .tappable(pressedScale = 0.95f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 11.dp),
    )
}

/**
 * The one piece of advice this screen gives, and it never leaves. [urgent] only changes how loudly
 * it says it: once a download has actually stopped moving, the card stops being a footnote.
 */
@Composable
private fun VpnNote(urgent: Boolean) {
    val tint = if (urgent) Palette.Gold else Palette.TextFaint

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER)
            .clip(CARD_SHAPE)
            .background(if (urgent) Palette.Gold.copy(alpha = 0.12f) else Palette.Surface)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.loadout_vpn_title),
                color = Palette.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.loadout_vpn_hint),
                color = Palette.TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun EmptyPage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(GUTTER),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Palette.SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOff,
                contentDescription = null,
                tint = Palette.TextFaint,
                modifier = Modifier.size(30.dp),
            )
        }
        Text(
            text = stringResource(R.string.loadout_empty),
            color = Palette.TextMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun LoadingPage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER),
        verticalArrangement = Arrangement.spacedBy(GAP),
    ) {
        // One card is a 44dp badge over two lines of text, inside 14dp of padding.
        repeat(3) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = CARD_SHAPE,
            )
        }
    }
}

/** The single line under a file's name. A running download shows [ProgressLine] instead. */
@Composable
private fun statusText(item: FileItem): String = when (val status = item.status) {
    FileStatus.Idle -> formatBytes(item.sizeBytes) ?: stringResource(R.string.loadout_unknown_size)
    is FileStatus.Downloading -> stringResource(R.string.loadout_percent, status.percent)
    FileStatus.Ready -> stringResource(R.string.loadout_ready)
}

private fun formatBytes(bytes: Long?): String? = when {
    bytes == null -> null
    bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.0f KB".format(bytes / 1_000.0)
    else -> "$bytes B"
}
