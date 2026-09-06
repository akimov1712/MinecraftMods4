package dev.mod.store.minecraft.feature.spotlight

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.mod.store.minecraft.core.ui.component.CategoryChip
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.component.ImageViewerDialog
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.RemoteImage
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.effect.SmallShape
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

private val SIDE_PADDING = 16.dp
private const val COLLAPSED_LINES = 6

private fun CreationEntity.images(): List<String> =
    (listOf(imageUrl) + gallery).filter { it.isNotBlank() }.distinct()

/**
 * The mod page reads as one continuous dossier rather than a set of tabs: cover, name, the two
 * things you can do with it, then the evidence — shots, description, files, versions — in the
 * order someone actually asks for them.
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

            creation == null -> ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item(key = "cover") {
                    Cover(
                        creation = creation,
                        onBack = component::back,
                        onToggleBookmark = { component.onIntent(Intent.ToggleBookmark) },
                        onOpenImage = { viewerIndex = it },
                    )
                }
                item(key = "facts") { FactStrip(creation) }
                item(key = "actions") { Actions(component) }

                gallerySection(creation) { viewerIndex = it }
                descriptionSection(creation)
                filesSection(creation, component)
                versionsSection(creation)

                item(key = "links") { FooterLinks(component) }
            }
        }

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
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

// region header

@Composable
private fun Cover(
    creation: CreationEntity,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenImage: (Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
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
                        0f to Palette.Canvas.copy(alpha = 0.5f),
                        0.35f to Color.Transparent,
                        1f to Palette.Canvas.copy(alpha = 0.95f),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                onClick = onBack,
                size = 38.dp,
                container = Palette.Canvas.copy(alpha = 0.6f),
            )
            GlassIconButton(
                icon = if (creation.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = null,
                onClick = onToggleBookmark,
                size = 38.dp,
                tint = if (creation.isBookmarked) Palette.Accent else Palette.TextPrimary,
                container = Palette.Canvas.copy(alpha = 0.6f),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = SIDE_PADDING, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryChip(creation.category)
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 21.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Rating, reactions, comments and age, set as one line of facts split by hairlines. */
@Composable
private fun FactStrip(creation: CreationEntity) {
    val facts = buildList {
        if (creation.rating > 0.0) add(Icons.Rounded.Star to formatRating(creation.rating))
        if (creation.reactionCount > 0) {
            add(Icons.Rounded.LocalFireDepartment to formatCompact(creation.reactionCount))
        }
        if (creation.commentCount > 0) add(Icons.Rounded.ChatBubble to formatCompact(creation.commentCount))
        creation.publishedAtEpochMs?.let { add(Icons.Rounded.Upload to formatRelativeTime(it)) }
    }
    if (facts.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        facts.forEachIndexed { index, (icon, value) ->
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Palette.TextFaint,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = value,
                    color = Palette.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
            if (index < facts.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Palette.Stroke),
                )
            }
        }
    }
}

@Composable
private fun Actions(component: SpotlightComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE_PADDING),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PillButton(
            text = stringResource(R.string.spotlight_get_files),
            onClick = component::openLoadout,
            modifier = Modifier.weight(1f),
            leading = {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = null,
                    tint = Palette.OnAccentDark,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
        PillButton(
            text = stringResource(R.string.spotlight_how_to_install),
            onClick = component::openWalkthrough,
            modifier = Modifier.weight(1f),
            container = Palette.SurfaceHigh,
            content = Palette.TextPrimary,
            leading = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                    contentDescription = null,
                    tint = Palette.TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            },
        )
    }
}

// endregion

// region body

private fun LazyListScope.gallerySection(creation: CreationEntity, onOpenImage: (Int) -> Unit) {
    val images = creation.images()
    if (images.size <= 1) return

    item(key = "gallery") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BlockTitle(
                text = stringResource(R.string.spotlight_tab_images),
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = SIDE_PADDING),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(images, key = { _, url -> url }) { index, url ->
                    RemoteImage(
                        url = url,
                        modifier = Modifier
                            .height(104.dp)
                            .width(150.dp)
                            .clip(SmallShape)
                            .tappable { onOpenImage(index) },
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.descriptionSection(creation: CreationEntity) {
    if (creation.description.isBlank()) return

    item(key = "description") {
        var expanded by remember(creation.id) { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BlockTitle(stringResource(R.string.spotlight_tab_about))
            Text(
                text = creation.description,
                color = Palette.TextMuted,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(
                    if (expanded) R.string.spotlight_read_less else R.string.spotlight_read_more,
                ),
                color = Palette.Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.tappable { expanded = !expanded },
            )
        }
    }
}

private fun LazyListScope.filesSection(creation: CreationEntity, component: SpotlightComponent) {
    if (creation.fileUrls.isEmpty()) return

    item(key = "files") {
        Column(
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BlockTitle(
                stringResource(R.string.spotlight_files_count, creation.fileUrls.size),
            )
            creation.fileUrls.forEachIndexed { index, url ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SmallShape)
                        .background(Palette.Surface)
                        .tappable { component.openLoadout() }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Palette.TextFaint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fileNameOf(url),
                            color = Palette.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = extensionOf(url, creation),
                            color = Palette.TextFaint,
                            fontSize = 12.sp,
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = Palette.Accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

private fun LazyListScope.versionsSection(creation: CreationEntity) {
    if (creation.supportedVersions.isEmpty()) return

    item(key = "versions") {
        Column(
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BlockTitle(stringResource(R.string.spotlight_versions_title))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                creation.supportedVersions.forEach { version ->
                    Text(
                        text = version,
                        color = Palette.TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(SmallShape)
                            .background(Palette.Surface)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterLinks(component: SpotlightComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE_PADDING),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FooterLink(
            icon = Icons.Rounded.Flag,
            text = stringResource(R.string.spotlight_report),
            onClick = { component.onIntent(Intent.OpenReport) },
            modifier = Modifier.weight(1f),
        )
        FooterLink(
            icon = Icons.Rounded.Upload,
            text = stringResource(R.string.spotlight_suggest),
            onClick = component::openOutreach,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FooterLink(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(SmallShape)
            .background(Palette.Surface)
            .tappable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Palette.TextMuted,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = text,
            color = Palette.TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BlockTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Palette.TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

// endregion

@Composable
private fun ReportDialog(
    form: SpotlightStore.ReportForm,
    component: SpotlightComponent,
) {
    Dialog(onDismissRequest = { component.onIntent(Intent.DismissReport) }) {
        Surface(color = Palette.SurfaceHigh, shape = SmallShape) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.spotlight_report_title),
                    color = Palette.TextPrimary,
                    fontSize = 17.sp,
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
