package dev.mod.store.minecraft.feature.spotlight

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.text.KeyboardOptions
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.ImageViewerDialog
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.RemoteImage
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.component.creationCategoryAccent
import dev.mod.store.minecraft.core.ui.component.creationCategoryLabel
import dev.mod.store.minecraft.core.ui.component.MetaChip
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.feature.spotlight.SpotlightStore.Intent

private fun CreationEntity.images(): List<String> =
    (listOf(imageUrl) + gallery).filter { it.isNotBlank() }.distinct()

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

            else -> ReadyContent(
                creation = creation,
                component = component,
                onOpenImage = { viewerIndex = it },
            )
        }

        RoundIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            onClick = component::back,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(12.dp),
        )

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
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

@Composable
private fun ReadyContent(
    creation: CreationEntity,
    component: SpotlightComponent,
    onOpenImage: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Carousel(images = creation.images(), onOpenImage = onOpenImage)

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CategoryBadge(creation)
                Text(
                    text = creation.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Palette.TextPrimary,
                )
                MetaRow(creation)
                Text(
                    text = creation.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Palette.TextMuted,
                )
                if (creation.supportedVersions.isNotEmpty()) {
                    VersionsSection(creation.supportedVersions)
                }
                TextButton(onClick = { component.onIntent(Intent.OpenReport) }) {
                    Text(stringResource(R.string.spotlight_report), color = Palette.AccentSoft)
                }
            }
        }
        BottomBar(creation = creation, component = component)
    }
}

@Composable
private fun Carousel(images: List<String>, onOpenImage: (Int) -> Unit) {
    if (images.isEmpty()) return
    val pager = rememberPagerState(pageCount = { images.size })
    HorizontalPager(
        state = pager,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)),
    ) { page ->
        RemoteImage(
            url = images[page],
            modifier = Modifier
                .fillMaxSize()
                .tappable { onOpenImage(page) },
            contentScale = ContentScale.Crop,
        )
    }
}

/** Rating, reactions and comments in one line under the title. */
@Composable
private fun MetaRow(creation: CreationEntity) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (creation.rating > 0.0) {
            MetaChip(
                text = formatRating(creation.rating),
                icon = Icons.Rounded.Star,
                tint = Palette.Gold,
            )
        }
        if (creation.reactionCount > 0) {
            MetaChip(
                text = formatCompact(creation.reactionCount),
                icon = Icons.Rounded.LocalFireDepartment,
                tint = Palette.Ember,
            )
        }
        if (creation.commentCount > 0) {
            MetaChip(
                text = formatCompact(creation.commentCount),
                icon = Icons.Rounded.ChatBubble,
                tint = Palette.Sky,
            )
        }
    }
}

@Composable
private fun CategoryBadge(creation: CreationEntity) {
    val accent = creationCategoryAccent(creation.category)
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = creationCategoryLabel(creation.category),
            style = MaterialTheme.typography.bodyMedium,
            color = accent,
        )
    }
}

@Composable
private fun VersionsSection(versions: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.spotlight_versions_title),
            style = MaterialTheme.typography.titleMedium,
            color = Palette.TextPrimary,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            versions.forEach { version ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clip(CircleShape)
                        .background(Palette.SurfaceHigh)
                        .border(1.dp, Palette.Stroke, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(version, style = MaterialTheme.typography.bodyMedium, color = Palette.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun BottomBar(creation: CreationEntity, component: SpotlightComponent) {
    Row(
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Palette.Surface.copy(alpha = 0.94f))
            .border(1.dp, Palette.GlassStroke, RoundedCornerShape(28.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundIconButton(
            icon = if (creation.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
            tint = if (creation.isBookmarked) Palette.Accent else Palette.TextPrimary,
            container = Palette.SurfaceHigh,
            onClick = { component.onIntent(Intent.ToggleBookmark) },
        )
        PillButton(
            text = stringResource(R.string.spotlight_how_to_install),
            onClick = component::openWalkthrough,
            modifier = Modifier.weight(1f),
            container = Palette.SurfaceHigh,
            content = Palette.TextPrimary,
        )
        PillButton(
            text = stringResource(R.string.spotlight_get_files),
            onClick = component::openLoadout,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Palette.TextPrimary,
    container: Color = Color.Black.copy(alpha = 0.45f),
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(container)
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ReportDialog(
    form: SpotlightStore.ReportForm,
    component: SpotlightComponent,
) {
    Dialog(onDismissRequest = { component.onIntent(Intent.DismissReport) }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Palette.SurfaceHigh,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.spotlight_report_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Palette.TextPrimary,
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
                        .height(120.dp),
                    placeholder = stringResource(R.string.spotlight_report_message_hint),
                    singleLine = false,
                    minLines = 3,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        onClick = { component.onIntent(Intent.DismissReport) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.spotlight_report_cancel), color = Palette.TextMuted)
                    }
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
