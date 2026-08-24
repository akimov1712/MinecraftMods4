package dev.mod.store.minecraft.feature.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.component.CreationCard
import dev.mod.store.minecraft.core.ui.component.CreationPoster
import dev.mod.store.minecraft.core.ui.component.CreationRow
import dev.mod.store.minecraft.core.ui.component.EmptyState
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.SectionHeader
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.effect.Appear
import dev.mod.store.minecraft.core.ui.effect.animatedCount
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatCount
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationFeed
import dev.mod.store.minecraft.domain.creation.HomeDigest
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Browse
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Intent

private const val CHART_SIZE = 5
private const val GRID_SIZE = 4
private const val VERSIONS_SHOWN = 12
private const val PRELOAD_DISTANCE = 3
private val SIDE_PADDING = 16.dp

/**
 * Home opens straight on the mod of the day — no greeting, no search field, no filters. Below it
 * the editorial sections follow one another; opening one in full swaps the whole feed for an
 * endless list over that section.
 */
@Composable
fun HomeFeed(
    state: ShowcaseStore.State,
    nativeAdInterval: Int,
    onIntent: (Intent) -> Unit,
    onOpenCreation: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val browse = state.browse
    val stage = state.stage

    PagingTrigger(listState, browse, onIntent)

    LaunchedEffect(browse?.feed) {
        if (browse != null) listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        when {
            browse != null -> browseContent(browse, nativeAdInterval, onIntent, onOpenCreation)

            stage is ScreenStage.Failed && state.digest.isEmpty -> item(key = "error") {
                ErrorState(
                    message = stage.message,
                    onRetry = { onIntent(Intent.Refresh) },
                    modifier = Modifier.padding(horizontal = SIDE_PADDING, vertical = 40.dp),
                )
            }

            state.digest.isEmpty && stage.isLoading -> digestSkeleton()

            else -> digestContent(state.digest, state, onIntent, onOpenCreation)
        }
    }
}

/** Loads the next page as the end of the browsing list comes into view. */
@Composable
private fun PagingTrigger(
    listState: LazyListState,
    browse: Browse?,
    onIntent: (Intent) -> Unit,
) {
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= info.totalItemsCount - PRELOAD_DISTANCE
        }
    }
    LaunchedEffect(nearEnd, browse?.stage, browse?.endReached, browse?.items?.size) {
        if (nearEnd && browse != null && !browse.stage.isLoading && !browse.stage.isFailed && !browse.endReached) {
            onIntent(Intent.LoadMore)
        }
    }
}

// region digest

private fun LazyListScope.digestContent(
    digest: HomeDigest,
    state: ShowcaseStore.State,
    onIntent: (Intent) -> Unit,
    onOpenCreation: (Int) -> Unit,
) {
    digest.pickOfDay?.let { pick ->
        item(key = "pick") {
            Appear(index = 0) {
                PickOfDayCard(
                    creation = pick,
                    onClick = { onOpenCreation(pick.id) },
                    modifier = Modifier.padding(horizontal = SIDE_PADDING),
                )
            }
        }
    }

    item(key = "stats") {
        Appear(index = 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SIDE_PADDING),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatTile(
                    value = formatCompact(animatedCount(digest.catalogTotal)),
                    label = stringResource(R.string.showcase_stat_catalog),
                    icon = Icons.Rounded.Inventory2,
                    accent = Palette.Sky,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = formatCount(animatedCount(state.bookmarkCount)),
                    label = stringResource(R.string.showcase_stat_saved),
                    icon = Icons.Rounded.Bookmark,
                    accent = Palette.Accent,
                    modifier = Modifier.weight(1f),
                )
                StatTile(
                    value = formatRating(digest.topRated.averageRating()),
                    label = stringResource(R.string.showcase_stat_rating),
                    icon = Icons.Rounded.Star,
                    accent = Palette.Gold,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (digest.trending.isNotEmpty()) {
        item(key = "trending") {
            Appear(index = 2) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_trending),
                        subtitle = stringResource(R.string.showcase_section_trending_subtitle),
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        accent = Palette.Accent,
                        actionLabel = stringResource(R.string.showcase_see_all),
                        onAction = { onIntent(Intent.OpenFeed(CreationFeed.Trending)) },
                        modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = SIDE_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(digest.trending, key = { _, creation -> creation.id }) { index, creation ->
                            CreationPoster(
                                creation = creation,
                                onClick = { onOpenCreation(creation.id) },
                                rank = index + 1,
                                accent = Palette.Accent,
                            )
                        }
                    }
                }
            }
        }
    }

    item(key = "ad_top") {
        NativeSlot(
            slotKey = "home_top",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
        )
    }

    if (digest.popular.isNotEmpty()) {
        item(key = "popular") {
            Appear(index = 0) {
                Column(
                    modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_popular),
                        subtitle = stringResource(R.string.showcase_section_popular_subtitle),
                        icon = Icons.Rounded.EmojiEvents,
                        accent = Palette.Gold,
                        actionLabel = stringResource(R.string.showcase_see_all),
                        onAction = { onIntent(Intent.OpenFeed(CreationFeed.Popular)) },
                    )
                    digest.popular.take(CHART_SIZE).forEachIndexed { index, creation ->
                        CreationRow(
                            creation = creation,
                            onClick = { onOpenCreation(creation.id) },
                            rank = index + 1,
                            trailing = { ChartMoveBadge(chartMove(creation, index, digest.trending)) },
                        )
                    }
                }
            }
        }
    }

    if (digest.fresh.isNotEmpty()) {
        item(key = "fresh") {
            Appear(index = 0) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_fresh),
                        subtitle = stringResource(R.string.showcase_section_fresh_subtitle),
                        icon = Icons.Rounded.NewReleases,
                        accent = Palette.Positive,
                        actionLabel = stringResource(R.string.showcase_see_all),
                        onAction = { onIntent(Intent.OpenFeed(CreationFeed.Fresh)) },
                        modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = SIDE_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(digest.fresh, key = { it.id }) { creation ->
                            FreshCard(creation = creation, onClick = { onOpenCreation(creation.id) })
                        }
                    }
                }
            }
        }
    }

    if (digest.topRated.isNotEmpty()) {
        item(key = "top_rated") {
            Appear(index = 0) {
                Column(
                    modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_top_rated),
                        subtitle = stringResource(R.string.showcase_section_top_rated_subtitle),
                        icon = Icons.Rounded.WorkspacePremium,
                        accent = Palette.Sky,
                        actionLabel = stringResource(R.string.showcase_see_all),
                        onAction = { onIntent(Intent.OpenFeed(CreationFeed.TopRated)) },
                    )
                    digest.topRated.take(GRID_SIZE).chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { creation ->
                                CreationPoster(
                                    creation = creation,
                                    onClick = { onOpenCreation(creation.id) },
                                    modifier = Modifier.weight(1f),
                                    width = null,
                                    aspectRatio = 0.86f,
                                    accent = Palette.Sky,
                                )
                            }
                            if (pair.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    item(key = "ad_bottom") {
        NativeSlot(
            slotKey = "home_bottom",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
        )
    }

    val versions = digest.everything.topVersions()
    if (versions.isNotEmpty()) {
        item(key = "versions") {
            VersionsStrip(versions = versions, modifier = Modifier.padding(horizontal = SIDE_PADDING))
        }
    }

    item(key = "tip") {
        TipCard(modifier = Modifier.padding(horizontal = SIDE_PADDING))
    }

    item(key = "footer") {
        FeedFooter(
            total = formatCount(digest.catalogTotal),
            modifier = Modifier.padding(horizontal = SIDE_PADDING, vertical = 8.dp),
        )
    }
}

/** Placeholder version of the digest, shown while the very first load is in flight. */
private fun LazyListScope.digestSkeleton() {
    item(key = "skeleton_hero") {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING)
                .aspectRatio(0.94f),
            shape = RoundedCornerShape(30.dp),
        )
    }
    item(key = "skeleton_stats") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(92.dp),
                    shape = RoundedCornerShape(20.dp),
                )
            }
        }
    }
    repeat(2) { index ->
        item(key = "skeleton_rail_$index") {
            Row(
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.72f),
                        shape = RoundedCornerShape(24.dp),
                    )
                }
            }
        }
    }
}

// endregion

// region one section in full

private fun LazyListScope.browseContent(
    browse: Browse,
    nativeAdInterval: Int,
    onIntent: (Intent) -> Unit,
    onOpenCreation: (Int) -> Unit,
) {
    item(key = "browse_head") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(browse.feed.titleRes()),
                    color = Palette.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (browse.total > 0) {
                    Text(
                        text = stringResource(R.string.showcase_browse_count, formatCount(browse.total)),
                        color = Palette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            GlassIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.showcase_browse_back),
                onClick = { onIntent(Intent.CloseBrowse) },
            )
        }
    }

    if (browse.items.isEmpty() && browse.stage.isLoading) {
        items(4, key = { index -> "browse_skeleton_$index" }) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SIDE_PADDING)
                    .aspectRatio(1.15f),
                shape = RoundedCornerShape(30.dp),
            )
        }
        return
    }

    browse.items.forEachIndexed { index, creation ->
        item(key = "browse_${creation.id}") {
            CreationCard(
                creation = creation,
                onClick = { onOpenCreation(creation.id) },
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
            )
        }
        if (nativeAdInterval > 0 && (index + 1) % nativeAdInterval == 0) {
            item(key = "browse_ad_$index") {
                NativeSlot(
                    slotKey = "browse_$index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SIDE_PADDING),
                )
            }
        }
    }

    item(key = "browse_footer") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
            contentAlignment = Alignment.Center,
        ) {
            val browseStage = browse.stage
            when {
                browseStage is ScreenStage.Failed && browse.items.isEmpty() -> ErrorState(
                    message = browseStage.message,
                    onRetry = { onIntent(Intent.Refresh) },
                )

                browseStage.isFailed -> PillButton(
                    text = stringResource(R.string.showcase_browse_retry),
                    onClick = { onIntent(Intent.LoadMore) },
                )

                browseStage.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Palette.Accent,
                )

                browse.items.isEmpty() -> EmptyState()

                else -> Box(Modifier.height(4.dp))
            }
        }
    }
}

internal fun CreationFeed.titleRes(): Int = when (this) {
    CreationFeed.Trending -> R.string.showcase_section_trending
    CreationFeed.Popular -> R.string.showcase_section_popular
    CreationFeed.TopRated -> R.string.showcase_section_top_rated
    CreationFeed.Fresh -> R.string.showcase_section_fresh
}

// endregion

private fun List<CreationEntity>.averageRating(): Double =
    filter { it.rating > 0.0 }.map { it.rating }.average().takeIf { !it.isNaN() } ?: 0.0

private fun List<CreationEntity>.topVersions(): List<String> =
    flatMap { it.supportedVersions }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenByDescending { it.key })
        .take(VERSIONS_SHOWN)
        .map { it.key }

/** Where a chart entry sits compared to the editorial order — the little arrow on its right. */
private fun chartMove(creation: CreationEntity, index: Int, trending: List<CreationEntity>): ChartMove {
    val trendingIndex = trending.indexOfFirst { it.id == creation.id }
    return when {
        trendingIndex < 0 -> ChartMove.New
        trendingIndex < index -> ChartMove.Up
        trendingIndex > index -> ChartMove.Down
        else -> ChartMove.Flat
    }
}
