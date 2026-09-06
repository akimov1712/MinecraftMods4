package dev.mod.store.minecraft.feature.spotlight

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.ImageViewerDialog
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.RemoteImage
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.component.creationCategoryAccent
import dev.mod.store.minecraft.core.ui.component.creationCategoryIcon
import dev.mod.store.minecraft.core.ui.component.creationCategoryLabel
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.core.ui.util.formatRelativeTime
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.feature.spotlight.SpotlightStore.Intent
import java.net.URLDecoder

private val SIDE_PADDING = 18.dp
private val SHEET_SHAPE = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
private const val COLLAPSED_LINES = 7

/** The pages the sheet can show. */
private enum class ModTab(val labelRes: Int, val icon: ImageVector) {
    About(R.string.spotlight_tab_about, Icons.Filled.Info),
    Files(R.string.spotlight_tab_files, Icons.Filled.Download),
    Images(R.string.spotlight_tab_images, Icons.Filled.Image),
    Versions(R.string.spotlight_tab_versions, Icons.Filled.Sell),
}

private fun CreationEntity.images(): List<String> =
    (listOf(imageUrl) + gallery).filter { it.isNotBlank() }.distinct()

/**
 * The mod fills the screen as a poster; the reading material rides over it on a sheet that can be
 * pushed up out of the way of the artwork. Tabs are a single sliding pill rather than a row of
 * buttons, and the one thing you came here to do — take the files — is a round key in the dock at
 * the bottom, not a banner across the page.
 */
@Composable
fun SpotlightPane(
    component: SpotlightComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }
    val reportSentText = stringResource(R.string.spotlight_report_sent)
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var tab by rememberSaveable { mutableStateOf(ModTab.About) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    ObserveSignals(component.labels) { label ->
        when (label) {
            is SpotlightStore.Label.Notify -> snackbar.showSnackbar(label.message)
            SpotlightStore.Label.ReportSent -> snackbar.showSnackbar(reportSentText)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val creation = state.creation
        when {
            state.stage is ScreenStage.Failed && creation == null ->
                ErrorState(
                    message = (state.stage as ScreenStage.Failed).message,
                    onRetry = { component.onIntent(Intent.Retry) },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )

            creation == null -> ShimmerBox(modifier = Modifier.fillMaxSize())

            else -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val sheetHeight = maxHeight * if (expanded) 0.94f else 0.52f

                Poster(
                    creation = creation,
                    identityBottomPadding = sheetHeight - 26.dp,
                    onBack = component::back,
                    onToggleBookmark = { component.onIntent(Intent.ToggleBookmark) },
                    onOpenImage = { viewerIndex = it },
                )

                Sheet(
                    creation = creation,
                    tab = tab,
                    expanded = expanded,
                    height = sheetHeight,
                    onSelectTab = { tab = it },
                    onToggleExpanded = { expanded = !expanded },
                    onOpenImage = { viewerIndex = it },
                    component = component,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .padding(horizontal = 16.dp),
        )
    }

    val viewerImages = state.creation?.images().orEmpty()
    viewerIndex?.let { index ->
        ImageViewerDialog(
            images = viewerImages,
            startIndex = index,
            onDismiss = { viewerIndex = null },
        )
    }

    if (state.reportOpen) {
        ReportDialog(form = state.report, component = component)
    }
}

// region poster

/** Artwork edge to edge, with the identity of the mod written straight onto it. */
@Composable
private fun Poster(
    creation: CreationEntity,
    identityBottomPadding: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenImage: (Int) -> Unit,
) {
    val images = creation.images()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.SurfaceHigh),
    ) {
        RemoteImage(
            url = creation.imageUrl,
            modifier = Modifier
                .fillMaxSize()
                .tappable { onOpenImage(0) },
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Palette.Canvas.copy(alpha = 0.55f),
                        0.3f to Color.Transparent,
                        1f to Palette.Canvas.copy(alpha = 0.85f),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GlassKey(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
            GlassKey(
                icon = if (creation.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                tint = if (creation.isBookmarked) Palette.Accent else Palette.TextPrimary,
                onClick = onToggleBookmark,
                modifier = if (creation.isBookmarked) Modifier.popIn() else Modifier,
            )
        }

        // A rail of the mod's own numbers, stacked down the right edge of the artwork.
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 70.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (creation.rating > 0.0) {
                RailStat(icon = Icons.Filled.Star, value = formatRating(creation.rating), tint = Palette.Gold)
            }
            if (creation.reactionCount > 0) {
                RailStat(
                    icon = Icons.Filled.LocalFireDepartment,
                    value = formatCompact(creation.reactionCount),
                    tint = Palette.Ember,
                )
            }
            if (creation.commentCount > 0) {
                RailStat(
                    icon = Icons.Filled.ChatBubble,
                    value = formatCompact(creation.commentCount),
                    tint = Palette.Sky,
                )
            }
            if (images.size > 1) {
                RailStat(icon = Icons.Filled.Image, value = images.size.toString(), tint = Palette.AccentSoft)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = SIDE_PADDING, end = SIDE_PADDING, bottom = identityBottomPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CategoryTag(creation)
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 27.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            creation.publishedAtEpochMs?.let { published ->
                Text(
                    text = stringResource(R.string.spotlight_published) + " · " + formatRelativeTime(published),
                    color = Palette.TextMuted,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun CategoryTag(creation: CreationEntity) {
    val accent = creationCategoryAccent(creation.category)
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Palette.Scrim)
            .border(1.dp, accent.copy(alpha = 0.6f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = creationCategoryIcon(creation.category),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = creationCategoryLabel(creation.category),
            color = accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun GlassKey(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Palette.TextPrimary,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Palette.Scrim)
            .border(1.dp, Palette.GlassStroke, CircleShape)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
    }
}

/** One number from the mod, printed on a small glass tile down the right edge. */
@Composable
private fun RailStat(icon: ImageVector, value: String, tint: Color) {
    Column(
        modifier = Modifier
            .width(46.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.Scrim)
            .border(1.dp, Palette.GlassStroke, RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Text(
            text = value,
            color = Palette.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

// endregion

// region sheet

@Composable
private fun Sheet(
    creation: CreationEntity,
    tab: ModTab,
    expanded: Boolean,
    height: androidx.compose.ui.unit.Dp,
    onSelectTab: (ModTab) -> Unit,
    onToggleExpanded: () -> Unit,
    onOpenImage: (Int) -> Unit,
    component: SpotlightComponent,
    modifier: Modifier = Modifier,
) {
    val animatedHeight by animateDpAsState(
        targetValue = height,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "sheet-height",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .clip(SHEET_SHAPE)
            .background(Palette.Surface)
            .border(1.dp, Palette.Stroke, SHEET_SHAPE),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .tappable(onClick = onToggleExpanded)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Palette.Stroke),
            )
        }

        TabSlider(active = tab, onSelect = onSelectTab)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (tab) {
                ModTab.About -> aboutPage(creation, component)
                ModTab.Files -> filesPage(creation, component)
                ModTab.Images -> imagesPage(creation, onOpenImage)
                ModTab.Versions -> versionsPage(creation)
            }
        }

        ActionDock(creation = creation, component = component)
    }
}

/** Four labels on one track with a single pill that slides between them. */
@Composable
private fun TabSlider(active: ModTab, onSelect: (ModTab) -> Unit) {
    val tabs = ModTab.entries
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE_PADDING),
    ) {
        val slotWidth = maxWidth / tabs.size
        val offset by animateDpAsState(
            targetValue = slotWidth * tabs.indexOf(active),
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "tab-offset",
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(CircleShape)
                .background(Palette.SurfaceHigh),
        )
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(slotWidth)
                .height(46.dp)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Palette.Accent),
        )
        Row(modifier = Modifier.height(46.dp)) {
            tabs.forEach { entry ->
                val selected = entry == active
                Row(
                    modifier = Modifier
                        .width(slotWidth)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .tappable(pressedScale = 0.95f) { onSelect(entry) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = entry.icon,
                        contentDescription = null,
                        tint = if (selected) Palette.OnAccentDark else Palette.TextFaint,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = stringResource(entry.labelRes),
                        color = if (selected) Palette.OnAccentDark else Palette.TextFaint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }
    }
}

/** What you came for: a round key, with the quieter actions beside it. */
@Composable
private fun ActionDock(creation: CreationEntity, component: SpotlightComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = SIDE_PADDING, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.spotlight_files_count, creation.fileUrls.size),
                color = Palette.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = creation.supportedVersions.firstOrNull()
                    ?.let { stringResource(R.string.spotlight_dock_version, it) }
                    ?: stringResource(R.string.spotlight_dock_ready),
                color = Palette.TextFaint,
                fontSize = 13.sp,
                maxLines = 1,
            )
        }

        GhostKey(icon = Icons.AutoMirrored.Filled.HelpOutline, onClick = component::openWalkthrough)
        GhostKey(icon = Icons.Filled.Flag, onClick = { component.onIntent(Intent.OpenReport) })

        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Palette.Accent)
                .tappable(pressedScale = 0.92f, onClick = component::openLoadout),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = stringResource(R.string.spotlight_get_files),
                tint = Palette.OnAccentDark,
                modifier = Modifier.size(27.dp),
            )
        }
    }
}

@Composable
private fun GhostKey(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Palette.SurfaceHigh)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Palette.TextMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}

// endregion

// region pages

private fun LazyListScope.aboutPage(creation: CreationEntity, component: SpotlightComponent) {
    item(key = "about") {
        var open by remember(creation.id) { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = creation.description,
                color = Palette.TextMuted,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                maxLines = if (open) Int.MAX_VALUE else COLLAPSED_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(if (open) R.string.spotlight_read_less else R.string.spotlight_read_more),
                color = Palette.AccentSoft,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.tappable { open = !open },
            )
        }
    }

    item(key = "suggest") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING)
                .clip(SmallShape)
                .background(Palette.SurfaceHigh)
                .tappable(onClick = component::openOutreach)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Upload,
                contentDescription = null,
                tint = Palette.AccentSoft,
                modifier = Modifier.size(19.dp),
            )
            Text(
                text = stringResource(R.string.spotlight_suggest),
                color = Palette.TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun LazyListScope.filesPage(creation: CreationEntity, component: SpotlightComponent) {
    itemsIndexed(creation.fileUrls, key = { _, url -> "file_$url" }) { index, url ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING)
                .clip(SmallShape)
                .background(Palette.SurfaceHigh)
                .tappable { component.openLoadout() }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Palette.Accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = (index + 1).toString(),
                    color = Palette.AccentSoft,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileNameOf(url),
                    color = Palette.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = extensionOf(url, creation),
                    color = Palette.TextFaint,
                    fontSize = 13.sp,
                )
            }
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                tint = Palette.Accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun LazyListScope.imagesPage(creation: CreationEntity, onOpenImage: (Int) -> Unit) {
    itemsIndexed(creation.images(), key = { _, url -> "shot_$url" }) { index, url ->
        RemoteImage(
            url = url,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING)
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(20.dp))
                .tappable { onOpenImage(index) },
            contentScale = ContentScale.Crop,
        )
    }
}

private fun LazyListScope.versionsPage(creation: CreationEntity) {
    item(key = "versions") {
        if (creation.supportedVersions.isEmpty()) {
            Text(
                text = stringResource(R.string.spotlight_versions_empty),
                color = Palette.TextMuted,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
            )
            return@item
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            creation.supportedVersions.forEach { version ->
                Text(
                    text = version,
                    color = Palette.AccentSoft,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Palette.Accent.copy(alpha = 0.16f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

// endregion

@Composable
private fun ReportDialog(
    form: SpotlightStore.ReportForm,
    component: SpotlightComponent,
) {
    Dialog(onDismissRequest = { component.onIntent(Intent.DismissReport) }) {
        Surface(color = Palette.SurfaceHigh, shape = RoundedCornerShape(24.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.spotlight_report_title),
                    color = Palette.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                OutlineField(
                    value = form.email,
                    onValueChange = { component.onIntent(Intent.ChangeReportEmail(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = stringResource(R.string.spotlight_report_email_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlineField(
                    value = form.message,
                    onValueChange = { component.onIntent(Intent.ChangeReportMessage(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = stringResource(R.string.spotlight_report_message_hint),
                    singleLine = false,
                    minLines = 3,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillButton(
                        text = stringResource(R.string.spotlight_report_cancel),
                        onClick = { component.onIntent(Intent.DismissReport) },
                        modifier = Modifier.weight(1f),
                        container = Palette.Surface,
                        content = Palette.TextPrimary,
                    )
                    PillButton(
                        text = stringResource(R.string.spotlight_report_send),
                        onClick = { component.onIntent(Intent.SubmitReport) },
                        modifier = Modifier.weight(1f),
                        enabled = form.canSubmit,
                        busy = form.sending,
                    )
                }
            }
        }
    }
}

private fun extensionOf(url: String, creation: CreationEntity): String {
    val suffix = fileNameOf(url).substringAfterLast('.', "")
    return (suffix.ifBlank { creation.category.fileExtension.removePrefix(".") }).lowercase()
}

private fun fileNameOf(url: String): String {
    val raw = url.substringAfterLast('/').substringBefore('?')
    return runCatching { URLDecoder.decode(raw, "UTF-8") }.getOrDefault(raw)
}
