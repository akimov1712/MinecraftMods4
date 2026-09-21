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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.NewReleases
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.AdCadence
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.BannerSkeleton
import dev.mod.store.minecraft.core.ui.component.CreationCard
import dev.mod.store.minecraft.core.ui.component.CreationCardSkeleton
import dev.mod.store.minecraft.core.ui.component.CreationPoster
import dev.mod.store.minecraft.core.ui.component.CreationRow
import dev.mod.store.minecraft.core.ui.component.CreationRowSkeleton
import dev.mod.store.minecraft.core.ui.component.EmptyState
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.RailSkeleton
import dev.mod.store.minecraft.core.ui.component.SectionHeader
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.effect.Appear
import dev.mod.store.minecraft.core.ui.effect.CardShape
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCount
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationFeed
import dev.mod.store.minecraft.domain.creation.HomeDigest
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Browse
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Intent

private const val CHART_SIZE = 5
private const val PRELOAD_DISTANCE = 3
private val SIDE_PADDING = 16.dp

/**
 * Home: the daily banner, a live strip of what is climbing, then tight rails and a chart. Rows
 * are sized so several sections are visible at once rather than one card per screenful.
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
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when {
            browse != null -> browseContent(browse, nativeAdInterval, onIntent, onOpenCreation)

            stage is ScreenStage.Failed && state.digest.isEmpty -> item(key = "error") {
                ErrorState(
                    message = stage.message,
                    onRetry = { onIntent(Intent.Refresh) },
                    modifier = Modifier.padding(horizontal = SIDE_PADDING, vertical = 32.dp),
                )
            }

            state.digest.isEmpty && stage.isLoading -> digestSkeleton()

            else -> digestContent(state.digest, onIntent, onOpenCreation)
        }
    }
}

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

// region sections

private fun LazyListScope.digestContent(
    digest: HomeDigest,
    onIntent: (Intent) -> Unit,
    onOpenCreation: (Int) -> Unit,
) {
    digest.pickOfDay?.let { pick ->
        item(key = "pick") {
            Appear(index = 1) {
                Column(
                    modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_pick),
                        icon = Icons.Rounded.AutoAwesome,
                        accent = Palette.Gold,
                    )
                    PickOfDayBanner(creation = pick, onClick = { onOpenCreation(pick.id) })
                }
            }
        }
    }

    if (digest.trending.isNotEmpty()) {
        item(key = "trending") {
            Appear(index = 2) {
                Rail(
                    title = stringResource(R.string.showcase_section_trending),
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    accent = Palette.Accent,
                    creations = digest.trending,
                    ranked = true,
                    onSeeAll = { onIntent(Intent.OpenFeed(CreationFeed.Trending)) },
                    onOpenCreation = onOpenCreation,
                )
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
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_popular),
                        icon = Icons.Rounded.EmojiEvents,
                        accent = Palette.Gold,
                        actionLabel = stringResource(R.string.showcase_see_all),
                        onAction = { onIntent(Intent.OpenFeed(CreationFeed.Popular)) },
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    digest.popular.take(CHART_SIZE).forEachIndexed { index, creation ->
                        CreationRow(
                            creation = creation,
                            onClick = { onOpenCreation(creation.id) },
                            rank = index + 1,
                            trailing = {
                                val move = chartMove(creation, index, digest.trending)
                                ChartMoveTag(move = move.first, delta = move.second)
                            },
                        )
                    }
                }
            }
        }
    }

    item(key = "ad_middle") {
        NativeSlot(
            slotKey = "home_middle",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
        )
    }

    if (digest.fresh.isNotEmpty()) {
        item(key = "fresh") {
            Appear(index = 0) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_fresh),
                        icon = Icons.Rounded.NewReleases,
                        accent = Palette.Positive,
                        actionLabel = stringResource(R.string.showcase_see_all),
                        onAction = { onIntent(Intent.OpenFeed(CreationFeed.Fresh)) },
                        modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = SIDE_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                Rail(
                    title = stringResource(R.string.showcase_section_top_rated),
                    icon = Icons.Rounded.WorkspacePremium,
                    accent = Palette.Sky,
                    creations = digest.topRated,
                    ranked = false,
                    onSeeAll = { onIntent(Intent.OpenFeed(CreationFeed.TopRated)) },
                    onOpenCreation = onOpenCreation,
                )
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
}

/** A titled row of tiles — the shape every rail shares. */
@Composable
private fun Rail(
    title: String,
    icon: ImageVector,
    accent: Color,
    creations: List<CreationEntity>,
    ranked: Boolean,
    onSeeAll: () -> Unit,
    onOpenCreation: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = title,
            icon = icon,
            accent = accent,
            actionLabel = stringResource(R.string.showcase_see_all),
            onAction = onSeeAll,
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = SIDE_PADDING),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(creations, key = { _, creation -> creation.id }) { index, creation ->
                CreationPoster(
                    creation = creation,
                    onClick = { onOpenCreation(creation.id) },
                    rank = if (ranked) index + 1 else null,
                    accent = accent,
                )
            }
        }
    }
}

private fun LazyListScope.digestSkeleton() {
    item(key = "skeleton_banner") {
        Column(
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(160.dp)
                    .height(26.dp),
                shape = SmallShape,
            )
            BannerSkeleton()
        }
    }

    item(key = "skeleton_rail") {
        RailSkeleton(modifier = Modifier.padding(horizontal = SIDE_PADDING))
    }

    item(key = "skeleton_chart") {
        Column(
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .width(140.dp)
                    .height(26.dp)
                    .padding(bottom = 6.dp),
                shape = SmallShape,
            )
            repeat(CHART_SIZE) { CreationRowSkeleton() }
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
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(browse.feed.titleRes()),
                    color = Palette.TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (browse.total > 0) {
                    Text(
                        text = stringResource(R.string.showcase_browse_count, formatCount(browse.total)),
                        color = Palette.TextFaint,
                        fontSize = 12.sp,
                    )
                }
            }
            GlassIconButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.showcase_browse_back),
                onClick = { onIntent(Intent.CloseBrowse) },
                size = 36.dp,
            )
        }
    }

    if (browse.items.isEmpty() && browse.stage.isLoading) {
        items(4, key = { index -> "browse_skeleton_$index" }) {
            CreationCardSkeleton(modifier = Modifier.padding(horizontal = SIDE_PADDING))
        }
        return
    }

    val cadence = AdCadence.of(nativeAdInterval)
    browse.items.forEachIndexed { index, creation ->
        item(key = "browse_${creation.id}") {
            CreationCard(
                creation = creation,
                onClick = { onOpenCreation(creation.id) },
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
                rank = index + 1,
            )
        }
        if (AdCadence.breaksAfter(index, cadence)) {
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

/** How far a mod sits from its editorial position — the delta shown next to a chart place. */
private fun chartMove(
    creation: CreationEntity,
    index: Int,
    trending: List<CreationEntity>,
): Pair<ChartMove, Int> {
    val trendingIndex = trending.indexOfFirst { it.id == creation.id }
    return when {
        trendingIndex < 0 -> ChartMove.New to 0
        trendingIndex < index -> ChartMove.Down to (index - trendingIndex)
        trendingIndex > index -> ChartMove.Up to (trendingIndex - index)
        else -> ChartMove.Flat to 0
    }
}
