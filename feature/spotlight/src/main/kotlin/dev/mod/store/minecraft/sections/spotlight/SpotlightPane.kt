package dev.mod.store.minecraft.feature.spotlight

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.CreationPoster
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.ImageViewerDialog
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.RefreshSurface
import dev.mod.store.minecraft.core.ui.component.RemoteImage
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.component.creationCategoryAccent
import dev.mod.store.minecraft.core.ui.component.creationCategoryIcon
import dev.mod.store.minecraft.core.ui.component.creationCategoryLabel
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatCount
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.core.ui.util.formatShortDate
import dev.mod.store.minecraft.core.ui.util.sharePlainText
import dev.mod.store.minecraft.core.ui.util.storeLink
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.reaction.ReactionSummary
import dev.mod.store.minecraft.domain.reaction.ReactionType
import dev.mod.store.minecraft.feature.spotlight.SpotlightStore.Intent
import java.net.URLDecoder

/** One gutter for the whole page, so every block starts on the same vertical line. */
private val GUTTER = 20.dp

/** The air between two blocks. Nothing on this page touches anything else. */
private val BLOCK_GAP = 14.dp

private val BLOCK_SHAPE = RoundedCornerShape(20.dp)
private val TILE_SHAPE = RoundedCornerShape(14.dp)
private val SHEET_SHAPE = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)

private const val DESCRIPTION_LINES = 6

/**
 * The readings of a mod. Each one owns a whole page; nothing from one chapter bleeds into the next,
 * which is the entire point of splitting them. A chapter with nothing to say does not get a tab —
 * see [chaptersOf].
 */
private enum class Chapter(val labelRes: Int) {
    Overview(R.string.spotlight_tab_about),
    Files(R.string.spotlight_tab_files),
    Shots(R.string.spotlight_tab_images),
    Versions(R.string.spotlight_tab_versions),
}

private fun CreationEntity.shots(): List<String> =
    (listOf(imageUrl) + gallery).filter { it.isNotBlank() }.distinct()

/**
 * The tabs this particular mod earns. Overview always has something; the rest appear only when the
 * author actually supplied the material, so the reader never opens an empty page.
 */
private fun chaptersOf(creation: CreationEntity): List<Chapter> = buildList {
    add(Chapter.Overview)
    if (creation.fileUrls.isNotEmpty()) add(Chapter.Files)
    if (creation.shots().isNotEmpty()) add(Chapter.Shots)
    if (creation.supportedVersions.isNotEmpty()) add(Chapter.Versions)
}

/**
 * A reading page, not a poster. Cover art on top, the mod's name under it, its numbers on one quiet
 * line, and then the download — high on the page, where the thing you came for belongs. Below that a
 * rail of chapters pins itself to the top as you scroll, and each chapter is built from titled
 * blocks with room around them, so the eye always knows where one thought ends and the next begins.
 */
@Composable
fun SpotlightPane(
    component: SpotlightComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val reportSentText = stringResource(R.string.spotlight_report_sent)
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    var chapter by rememberSaveable { mutableStateOf(Chapter.Overview) }

    ObserveSignals(component.labels) { label ->
        when (label) {
            is SpotlightStore.Label.Notify -> snackbar.showSnackbar(label.message)
            SpotlightStore.Label.ReportSent -> snackbar.showSnackbar(reportSentText)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.Canvas),
    ) {
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

            creation == null -> LoadingPage()

            else -> {
                val chapters = remember(creation) { chaptersOf(creation) }

                RefreshSurface(
                    refreshing = state.refreshing,
                    onRefresh = { component.onIntent(Intent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Story(
                        creation = creation,
                        reactions = state.reactions,
                        pendingReaction = state.pendingReaction,
                        onReact = { component.onIntent(Intent.React(it)) },
                        chapters = chapters,
                        // A tab can vanish when the mod reloads with less material; fall back to the
                        // one chapter that is always there rather than showing a blank page.
                        chapter = chapter.takeIf { it in chapters } ?: Chapter.Overview,
                        onSelectChapter = { chapter = it },
                        onOpenShot = { viewerIndex = it },
                        component = component,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        if (creation != null) {
            val context = LocalContext.current
            val shareTemplate = stringResource(R.string.spotlight_share_text)
            TopControls(
                bookmarked = creation.isBookmarked,
                onBack = component::back,
                onToggleBookmark = { component.onIntent(Intent.ToggleBookmark) },
                onShare = {
                    sharePlainText(
                        context = context,
                        text = shareTemplate.format(creation.title, storeLink(context)),
                    )
                },
            )
        }

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
                .padding(horizontal = 16.dp),
        )
    }

    val shots = state.creation?.shots().orEmpty()
    viewerIndex?.let { index ->
        ImageViewerDialog(
            images = shots,
            startIndex = index,
            onDismiss = { viewerIndex = null },
        )
    }

    if (state.reportOpen) {
        ReportSheet(form = state.report, component = component)
    }
}

// region page

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Story(
    creation: CreationEntity,
    reactions: ReactionSummary,
    pendingReaction: ReactionType?,
    onReact: (ReactionType) -> Unit,
    chapters: List<Chapter>,
    chapter: Chapter,
    onSelectChapter: (Chapter) -> Unit,
    onOpenShot: (Int) -> Unit,
    component: SpotlightComponent,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item(key = "cover") {
            Cover(creation = creation, onOpen = { onOpenShot(0) })
        }

        item(key = "identity") {
            Identity(creation)
        }

        item(key = "facts") {
            Spacer(Modifier.height(10.dp))
            FactLine(creation)
        }

        item(key = "get") {
            Spacer(Modifier.height(18.dp))
            GetFiles(creation = creation, onClick = component::openLoadout)
        }

        item(key = "reactions") {
            Spacer(Modifier.height(16.dp))
            ReactionStrip(summary = reactions, pending = pendingReaction, onReact = onReact)
        }

        // A lone Overview tab is not a choice, so the rail stays out of the way entirely.
        if (chapters.size > 1) {
            stickyHeader(key = "rail") {
                ChapterRail(chapters = chapters, active = chapter, onSelect = onSelectChapter)
            }
        } else {
            item(key = "rail_gap") { Spacer(Modifier.height(BLOCK_GAP)) }
        }

        when (chapter) {
            Chapter.Overview -> overviewChapter(creation, component)
            Chapter.Files -> filesChapter(creation, component)
            Chapter.Shots -> shotsChapter(creation, onOpenShot)
            Chapter.Versions -> versionsChapter(creation)
        }

        item(key = "tail") { Spacer(Modifier.navigationBarsPadding()) }
    }
}

/** Cover art, sized so it reads as a picture rather than swallowing the screen. */
@Composable
private fun Cover(creation: CreationEntity, onOpen: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.35f)
            .background(Palette.Surface)
            .tappable(pressedScale = 1f, onClick = onOpen),
    ) {
        RemoteImage(
            url = creation.imageUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // Ink at the top so the floating keys stay legible, ink at the foot so the artwork hands
        // over to the page background instead of stopping on a hard edge.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Palette.Canvas.copy(alpha = 0.70f),
                        0.28f to Color.Transparent,
                        0.78f to Color.Transparent,
                        1f to Palette.Canvas,
                    ),
                ),
        )

        val shots = creation.shots()
        if (shots.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = GUTTER, bottom = 14.dp)
                    .clip(CircleShape)
                    .background(Palette.Scrim)
                    .border(1.dp, Palette.GlassStroke, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null,
                    tint = Palette.TextPrimary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = shots.size.toString(),
                    color = Palette.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Who this mod is: its kind, its name, when it landed — on plain ink, nothing competing. */
@Composable
private fun Identity(creation: CreationEntity) {
    val accent = creationCategoryAccent(creation.category)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f))
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

            creation.trendingPosition?.let { place -> TrendingBadge(place) }
        }

        Text(
            text = creation.title,
            color = Palette.TextPrimary,
            fontSize = 26.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * The mod's numbers as one quiet line under the title — a star and a figure, a flame and a figure,
 * the date. No panel, no captions, no cells: these are things you glance at on the way past, and a
 * boxed grid of them was reading as the most important object on the page, which it is not.
 */
@Composable
private fun FactLine(creation: CreationEntity) {
    val facts = buildList {
        if (creation.rating > 0.0) {
            add(Icons.Filled.Star to formatRating(creation.rating) to Palette.Gold)
        }
        if (creation.reactionCount > 0) {
            add(Icons.Filled.LocalFireDepartment to formatCompact(creation.reactionCount) to Palette.Ember)
        }
        if (creation.commentCount > 0) {
            add(Icons.Filled.ChatBubbleOutline to formatCompact(creation.commentCount) to Palette.Sky)
        }
        if (creation.downloadsCount > 0) {
            add(Icons.Filled.Download to formatCompact(creation.downloadsCount) to Palette.Positive)
        }
    }
    val published = creation.publishedAtEpochMs

    if (facts.isEmpty() && published == null) return

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        facts.forEach { (pair, tint) ->
            val (icon, value) = pair
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = value,
                    color = Palette.TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        published?.let {
            Text(
                text = formatShortDate(it),
                color = Palette.TextFaint,
                fontSize = 14.sp,
            )
        }
    }
}

/**
 * The reason the page exists, said once and said plainly, high enough that it is the first thing
 * under the name rather than a strip clinging to the bottom of the screen.
 */
@Composable
private fun GetFiles(creation: CreationEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER)
            .clip(CircleShape)
            .background(Palette.Accent)
            .tappable(pressedScale = 0.97f, onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Download,
            contentDescription = null,
            tint = Palette.OnAccentDark,
            modifier = Modifier.size(21.dp),
        )
        Text(
            text = stringResource(R.string.spotlight_get_files),
            color = Palette.OnAccentDark,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 9.dp),
        )
    }
}

/**
 * The mod's place in the trending chart, shown only when it has one. Worded as a place ("#7 in
 * trending") rather than a number on its own, and tinted in the accent because it is a boast.
 */
@Composable
private fun TrendingBadge(place: Int) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Palette.Accent.copy(alpha = 0.14f))
            .border(1.dp, Palette.Accent.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = Palette.AccentSoft,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = stringResource(R.string.spotlight_trending_badge, place),
            color = Palette.AccentSoft,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** The reactions on offer, in the order the backend lists them, each with its face. */
private val REACTIONS = listOf(
    ReactionType.LIKE to "\uD83D\uDC4D",
    ReactionType.FIRE to "\uD83D\uDD25",
    ReactionType.LOVE to "\u2764\uFE0F",
    ReactionType.FUNNY to "\uD83D\uDE02",
    ReactionType.WOW to "\uD83D\uDE2E",
)

private fun ReactionType.labelRes(): Int = when (this) {
    ReactionType.LIKE -> R.string.reaction_like
    ReactionType.FIRE -> R.string.reaction_fire
    ReactionType.LOVE -> R.string.reaction_love
    ReactionType.FUNNY -> R.string.reaction_funny
    ReactionType.WOW -> R.string.reaction_wow
}

/**
 * Five reactions in a row, each a face over its count. The one this reader chose is lit; tapping
 * it again takes it back, tapping another moves the choice.
 *
 * While a change is on its way, the key that was tapped swaps its count for a spinner, so it is
 * obvious that something is happening and where. The other keys stay at full brightness — a whole
 * row going grey looked like the screen had frozen — and simply ignore taps until the write lands.
 */
@Composable
private fun ReactionStrip(
    summary: ReactionSummary,
    pending: ReactionType?,
    onReact: (ReactionType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.spotlight_reactions_title),
            color = Palette.TextFaint,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            REACTIONS.forEach { (type, face) ->
                ReactionKey(
                    face = face,
                    label = stringResource(type.labelRes()),
                    count = summary.count(type),
                    selected = summary.selected == type,
                    loading = pending == type,
                    enabled = pending == null,
                    onClick = { onReact(type) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReactionKey(
    face: String,
    label: String,
    count: Int,
    selected: Boolean,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border by animateColorAsState(
        targetValue = if (selected) Palette.Accent else Palette.Stroke,
        label = "reaction-border",
    )
    val fill by animateColorAsState(
        targetValue = if (selected) Palette.Accent.copy(alpha = 0.16f) else Palette.Surface,
        label = "reaction-fill",
    )

    Column(
        modifier = modifier
            .clip(TILE_SHAPE)
            .background(fill)
            .border(if (selected) 1.5.dp else 1.dp, border, TILE_SHAPE)
            .semantics { contentDescription = label }
            .tappable(enabled = enabled, pressedScale = 0.92f, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = face,
            fontSize = 22.sp,
            modifier = Modifier.then(if (selected) Modifier.popIn() else Modifier),
        )
        // A fixed-height slot, so swapping the count for the spinner never moves the row.
        Box(
            modifier = Modifier.height(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = Palette.Accent,
                    trackColor = Palette.Accent.copy(alpha = 0.18f),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = formatCompact(count),
                    color = if (selected) Palette.TextPrimary else Palette.TextFaint,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * The chapter names this mod earns, on one pinned rail. Words only, evenly split, a bar under the
 * live one — a tab has to be readable before it is clever.
 */
@Composable
private fun ChapterRail(
    chapters: List<Chapter>,
    active: Chapter,
    onSelect: (Chapter) -> Unit,
) {
    Column(modifier = Modifier.background(Palette.Canvas)) {
        Spacer(Modifier.height(BLOCK_GAP))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GUTTER),
        ) {
            val slot = maxWidth / chapters.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                chapters.forEach { entry ->
                    val live = entry == active
                    Column(
                        modifier = Modifier
                            .width(slot)
                            .fillMaxHeight()
                            .tappable(pressedScale = 0.94f) { onSelect(entry) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(entry.labelRes),
                            color = if (live) Palette.TextPrimary else Palette.TextFaint,
                            fontSize = 15.sp,
                            fontWeight = if (live) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(if (live) 26.dp else 0.dp)
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(Palette.Accent),
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Palette.Stroke),
        )
        Spacer(Modifier.height(BLOCK_GAP))
    }
}

// endregion

// region blocks

/** A titled panel. Everything inside a chapter arrives in one of these. */
@Composable
private fun Block(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER)
            .clip(BLOCK_SHAPE)
            .background(Palette.Surface)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Palette.AccentSoft,
                modifier = Modifier.size(19.dp),
            )
            Text(
                text = title,
                color = Palette.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        content()
    }
}

/** `label ————— value`, ruled off from the next one. The readable way to state a fact. */
@Composable
private fun Fact(label: String, value: String, last: Boolean = false) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 11.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = label,
                color = Palette.TextFaint,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                color = Palette.TextPrimary,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1.3f),
            )
        }
        if (!last) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Palette.Stroke),
            )
        }
    }
}

/** A row you can press: a glyph, what it is, what it does, a chevron. */
@Composable
private fun LinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER)
            .clip(TILE_SHAPE)
            .background(Palette.Surface)
            .tappable(pressedScale = 0.98f, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                color = Palette.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                color = Palette.TextFaint,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = Palette.TextFaint,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** Nothing to show on this chapter — said plainly, in the middle of the space it would fill. */
@Composable
private fun BlankSlate(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GUTTER)
            .clip(BLOCK_SHAPE)
            .background(Palette.Surface)
            .padding(horizontal = 24.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Palette.SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Palette.TextFaint,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = message,
            color = Palette.TextMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )
    }
}

// endregion

// region chapters

private fun LazyListScope.overviewChapter(
    creation: CreationEntity,
    component: SpotlightComponent,
) {
    item(key = "overview_text") {
        var open by remember(creation.id) { mutableStateOf(false) }
        Block(
            title = stringResource(R.string.spotlight_section_description),
            icon = Icons.Outlined.Description,
            modifier = Modifier.animateContentSize(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = creation.description,
                    color = Palette.TextMuted,
                    fontSize = 15.sp,
                    lineHeight = 25.sp,
                    maxLines = if (open) Int.MAX_VALUE else DESCRIPTION_LINES,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .tappable(pressedScale = 0.96f) { open = !open }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (open) R.string.spotlight_read_less else R.string.spotlight_read_more,
                        ),
                        color = Palette.AccentSoft,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Icon(
                        imageVector = if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = Palette.AccentSoft,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
        }
    }

    // Once the description has been read — the natural pause in the chapter.
    overviewAd(component, key = "overview_ad_description", slotKey = "spotlight_after_description")

    item(key = "overview_facts") {
        Spacer(Modifier.height(BLOCK_GAP))
        val none = stringResource(R.string.spotlight_value_none)
        Block(
            title = stringResource(R.string.spotlight_section_details),
            icon = Icons.Outlined.Tune,
        ) {
            Column {
                Fact(
                    label = stringResource(R.string.spotlight_detail_category),
                    value = creationCategoryLabel(creation.category),
                )
                Fact(
                    label = stringResource(R.string.spotlight_detail_format),
                    value = creation.category.fileExtension,
                )
                Fact(
                    label = stringResource(R.string.spotlight_detail_files),
                    value = creation.fileUrls.size.toString(),
                )
                Fact(
                    label = stringResource(R.string.spotlight_detail_versions),
                    value = creation.supportedVersions.take(3).joinToString(", ").ifBlank { none },
                )
                if (creation.downloadsCount > 0) {
                    Fact(
                        label = stringResource(R.string.spotlight_detail_downloads),
                        value = formatCount(creation.downloadsCount),
                    )
                }
                Fact(
                    label = stringResource(R.string.spotlight_detail_published),
                    value = creation.publishedAtEpochMs?.let { formatShortDate(it) } ?: none,
                    last = true,
                )
            }
        }
    }

    item(key = "overview_install") {
        Spacer(Modifier.height(BLOCK_GAP))
        LinkRow(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = stringResource(R.string.spotlight_how_to_install),
            subtitle = stringResource(R.string.spotlight_install_sub),
            tint = Palette.Sky,
            onClick = component::openWalkthrough,
        )
    }

    item(key = "overview_suggest") {
        Spacer(Modifier.height(10.dp))
        LinkRow(
            icon = Icons.Filled.Upload,
            title = stringResource(R.string.spotlight_suggest),
            subtitle = stringResource(R.string.spotlight_suggest_sub),
            tint = Palette.Violet,
            onClick = component::openOutreach,
        )
    }

    item(key = "overview_report") {
        Spacer(Modifier.height(10.dp))
        LinkRow(
            icon = Icons.Filled.Flag,
            title = stringResource(R.string.spotlight_report),
            subtitle = stringResource(R.string.spotlight_report_sub),
            tint = Palette.Negative,
            onClick = { component.onIntent(Intent.OpenReport) },
        )
    }

    overviewAd(component, key = "overview_ad_similar", slotKey = "spotlight_before_similar")

    // What to open next, once this page has been read. Only mods the server picked from the same
    // app, and never this one, so every tile here is a real next step.
    if (creation.similar.isNotEmpty()) {
        item(key = "overview_similar") {
            Spacer(Modifier.height(BLOCK_GAP + 6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = GUTTER),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Palette.AccentSoft,
                        modifier = Modifier.size(19.dp),
                    )
                    Text(
                        text = stringResource(R.string.spotlight_section_similar),
                        color = Palette.TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = GUTTER),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(creation.similar, key = { it.id }) { similar ->
                        CreationPoster(
                            creation = similar,
                            onClick = { component.openCreation(similar.id) },
                            width = 150.dp,
                        )
                    }
                }
            }
        }
    }

}

/**
 * One ad slot inside the About chapter. The other chapters — files, screenshots, versions — never
 * get one: there the reader is choosing a file or looking at pictures, not reading, and an ad would
 * sit in the middle of the thing they came to do.
 */
private fun LazyListScope.overviewAd(
    component: SpotlightComponent,
    key: String,
    slotKey: String,
) {
    if (!component.hasNativeAd) return
    item(key = key) {
        Spacer(Modifier.height(BLOCK_GAP))
        NativeSlot(
            slotKey = slotKey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GUTTER),
        )
    }
}

private fun LazyListScope.filesChapter(
    creation: CreationEntity,
    component: SpotlightComponent,
) {
    if (creation.fileUrls.isEmpty()) {
        item(key = "files_none") {
            BlankSlate(
                icon = Icons.Outlined.FolderOpen,
                message = stringResource(R.string.spotlight_files_empty),
            )
        }
        return
    }

    item(key = "files_hint") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GUTTER)
                .clip(TILE_SHAPE)
                .background(Palette.Sky.copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Palette.Sky,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.spotlight_files_hint),
                color = Palette.TextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
        Spacer(Modifier.height(BLOCK_GAP))
    }

    itemsIndexed(creation.fileUrls, key = { _, url -> "file_$url" }) { index, url ->
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GUTTER)
                    .clip(TILE_SHAPE)
                    .background(Palette.Surface)
                    .tappable(pressedScale = 0.98f) { component.openLoadout() }
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(TILE_SHAPE)
                        .background(Palette.Accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Palette.AccentSoft,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = fileNameOf(url),
                        color = Palette.TextPrimary,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = extensionOf(url, creation).uppercase(),
                        color = Palette.TextFaint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Palette.Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        tint = Palette.OnAccentDark,
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

private fun LazyListScope.shotsChapter(
    creation: CreationEntity,
    onOpenShot: (Int) -> Unit,
) {
    val shots = creation.shots()
    if (shots.isEmpty()) {
        item(key = "shots_none") {
            BlankSlate(
                icon = Icons.Filled.ImageNotSupported,
                message = stringResource(R.string.spotlight_images_empty),
            )
        }
        return
    }

    itemsIndexed(shots, key = { _, url -> "shot_$url" }) { index, url ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GUTTER),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RemoteImage(
                url = url,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(BLOCK_SHAPE)
                    .tappable(pressedScale = 0.98f) { onOpenShot(index) },
                contentScale = ContentScale.Crop,
            )
            Text(
                text = stringResource(R.string.spotlight_image_index, index + 1, shots.size),
                color = Palette.TextFaint,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(BLOCK_GAP))
        }
    }
}

private fun LazyListScope.versionsChapter(creation: CreationEntity) {
    if (creation.supportedVersions.isEmpty()) {
        item(key = "versions_none") {
            BlankSlate(
                icon = Icons.Outlined.Info,
                message = stringResource(R.string.spotlight_versions_empty),
            )
        }
        return
    }

    item(key = "versions_list") {
        Block(
            title = stringResource(R.string.spotlight_versions_title),
            icon = Icons.Outlined.Info,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(R.string.spotlight_versions_hint),
                    color = Palette.TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    creation.supportedVersions.forEach { version ->
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Palette.SurfaceHigh)
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Palette.Positive,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = version,
                                color = Palette.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// endregion

// region chrome

/** Back and bookmark float over the cover art on glass, out of the way of the reading. */
@Composable
private fun BoxScope.TopControls(
    bookmarked: Boolean,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        GlassKey(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassKey(
                icon = Icons.Filled.Share,
                onClick = onShare,
                contentDescription = stringResource(R.string.spotlight_share),
            )
            GlassKey(
                icon = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                tint = if (bookmarked) Palette.Accent else Palette.TextPrimary,
                onClick = onToggleBookmark,
            )
        }
    }
}

/**
 * A key floating on the artwork: a disc of smoked glass with one glyph on it.
 *
 * The disc is painted by [drawBehind] placed *after* `tappable`, which matters. `tappable` scales
 * through a graphics layer, and any `background` declared before it is drawn outside that layer —
 * so on press the glyph shrinks while the disc stays put, and the two come apart. Drawing the disc
 * after the press modifier puts both inside the same layer: they move together and the dark disc is
 * on screen in every state, pressed, saved or idle.
 */
@Composable
private fun GlassKey(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color = Palette.TextPrimary,
    contentDescription: String? = null,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .tappable(onClick = onClick)
            .drawBehind {
                val radius = size.minDimension / 2f
                drawCircle(color = Palette.Scrim, radius = radius)
                drawCircle(
                    color = Palette.GlassStroke,
                    radius = radius - 0.5.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(21.dp),
        )
    }
}

/** The page's own shape, drawn in grey, while the mod is on its way. */
@Composable
private fun LoadingPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(BLOCK_GAP),
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.35f),
        )
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = GUTTER)
                .fillMaxWidth(0.55f)
                .height(28.dp),
            shape = TILE_SHAPE,
        )
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = GUTTER)
                .fillMaxWidth()
                .height(72.dp),
            shape = BLOCK_SHAPE,
        )
        ShimmerBox(
            modifier = Modifier
                .padding(horizontal = GUTTER)
                .fillMaxWidth()
                .height(170.dp),
            shape = BLOCK_SHAPE,
        )
    }
}

// endregion

/**
 * Reporting a problem, as a sheet that rises from the foot of the screen rather than a grey box
 * dropped in the middle of it. It opens with a line saying what the report is for, so the reader
 * knows what to write; the fields are labelled; and the two ways out — close, or send — are a
 * dismiss key in the corner and one wide button, never two buttons of equal weight fighting for
 * the same tap.
 */
@Composable
private fun ReportSheet(
    form: SpotlightStore.ReportForm,
    component: SpotlightComponent,
) {
    Dialog(
        onDismissRequest = { component.onIntent(Intent.DismissReport) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .tappable(pressedScale = 1f) { component.onIntent(Intent.DismissReport) },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SHEET_SHAPE)
                    .background(Palette.Surface)
                    .border(1.dp, Palette.Stroke, SHEET_SHAPE)
                    // Swallows taps so pressing inside the sheet never closes it.
                    .tappable(pressedScale = 1f) {}
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = GUTTER, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Palette.Stroke),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Palette.Negative.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = null,
                            tint = Palette.Negative,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.spotlight_report_title),
                            color = Palette.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.spotlight_report_body),
                            color = Palette.TextFaint,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Palette.SurfaceHigh)
                            .tappable { component.onIntent(Intent.DismissReport) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.spotlight_report_cancel),
                            tint = Palette.TextMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        text = stringResource(R.string.spotlight_report_email_label),
                        color = Palette.TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlineField(
                        value = form.email,
                        onValueChange = { component.onIntent(Intent.ChangeReportEmail(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = stringResource(R.string.spotlight_report_email_hint),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(
                        text = stringResource(R.string.spotlight_report_message_label),
                        color = Palette.TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    OutlineField(
                        value = form.message,
                        onValueChange = { component.onIntent(Intent.ChangeReportMessage(it)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = stringResource(R.string.spotlight_report_message_hint),
                        singleLine = false,
                        minLines = 3,
                    )
                }

                PillButton(
                    text = stringResource(R.string.spotlight_report_send),
                    onClick = { component.onIntent(Intent.SubmitReport) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = form.canSubmit,
                    busy = form.sending,
                )
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
