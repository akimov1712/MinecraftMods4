package dev.mod.store.minecraft.feature.showcase

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.AdCadence
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.CreationCard
import dev.mod.store.minecraft.core.ui.component.CreationCardSkeleton
import dev.mod.store.minecraft.core.ui.component.EmptyState
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.creationCategoryLabel
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.domain.config.AdPlacement
import dev.mod.store.minecraft.domain.creation.CreationCategory
import dev.mod.store.minecraft.domain.creation.CreationFeed
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Intent

internal val SIDE_PADDING = 16.dp

/** How close to the foot of the list the next page is asked for. */
private const val PRELOAD_DISTANCE = 3

private const val SKELETON_CARDS = 3

private val KeyShape = RoundedCornerShape(14.dp)
private val TileShape = RoundedCornerShape(14.dp)

/** The orders offered, in the order the panel lists them. */
private val SORTS = listOf(
    CreationFeed.Trending,
    CreationFeed.Popular,
    CreationFeed.Fresh,
    CreationFeed.TopRated,
)

/**
 * Home: one list of the catalog, and a single strip above it.
 *
 * The strip holds no controls of its own — it is a sentence saying how the list is currently cut
 * ("Maps · Newest"), with a key at each end: one opens search, the other raises the panel where
 * that sentence is edited. Everything that used to be a row of chips and a drop-down now lives in
 * that panel, so the page itself stays a page of mods rather than a page of settings.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeFeed(
    state: ShowcaseStore.State,
    nativeAdInterval: Int,
    onIntent: (Intent) -> Unit,
    onOpenCreation: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var panelOpen by remember { mutableStateOf(false) }

    PagingTrigger(listState, state, onIntent)

    // A new cut is a new list: start it from the top rather than halfway down the old one.
    LaunchedEffect(state.category, state.sort) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        stickyHeader(key = "controls") {
            Controls(
                state = state,
                onPickCategory = { onIntent(Intent.ChangeCategory(it)) },
                onOpenOrders = { panelOpen = true },
            )
        }

        when {
            state.items.isNotEmpty() -> catalog(state, nativeAdInterval, onOpenCreation)

            state.stage.isLoading -> items(SKELETON_CARDS, key = { index -> "skeleton_$index" }) {
                CreationCardSkeleton(modifier = Modifier.padding(horizontal = SIDE_PADDING))
            }

            else -> Unit
        }

        item(key = "footer") {
            Footer(state = state, onIntent = onIntent)
        }
    }

    if (panelOpen) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { panelOpen = false },
            sheetState = sheetState,
            containerColor = Palette.Surface,
            contentColor = Palette.TextPrimary,
            scrimColor = Palette.Scrim,
        ) {
            OrderPanel(
                current = state.sort,
                onPick = { sort ->
                    onIntent(Intent.ChangeSort(sort))
                    panelOpen = false
                },
            )
        }
    }
}

// region header

/**
 * One line above the list: the kinds of content as tabs, and the order they come in.
 *
 * Nothing announces the screen — a reader who opened the catalogue knows they are in it, and a
 * running total tells them nothing they can act on. The tabs scroll sideways and pass under the
 * order key, which keeps its place at the end of the line.
 */
@Composable
private fun Controls(
    state: ShowcaseStore.State,
    onPickCategory: (CreationCategory?) -> Unit,
    onOpenOrders: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.Canvas)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TypeTabs(selected = state.category, onPick = onPickCategory)
                // Whatever tab the row ends on dissolves into the background rather than being
                // sliced through, so the row reads as something that keeps going.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                0.86f to Color.Transparent,
                                1f to Palette.Canvas,
                            ),
                        ),
                )
            }
            OrderKey(
                current = state.sort,
                onClick = onOpenOrders,
                modifier = Modifier.padding(start = 6.dp, end = SIDE_PADDING),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Palette.Stroke),
        )
    }
}

/** The current order on a cut tile; the rest are a tap away. */
@Composable
private fun OrderKey(
    current: CreationFeed,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(KeyShape)
            .tappable(pressedScale = 0.94f, onClick = onClick)
            .background(Palette.Surface)
            .border(1.dp, Palette.Stroke, KeyShape)
            .padding(start = 12.dp, end = 11.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Text(
            text = stringResource(current.titleRes()),
            color = Palette.Accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
        Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = stringResource(R.string.showcase_filter_order),
            tint = Palette.Accent,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** Kinds of content as tabs: a word, and a torch bar under the one in force. */
@Composable
private fun TypeTabs(
    selected: CreationCategory?,
    onPick: (CreationCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = SIDE_PADDING),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item(key = "all") {
            TypeTab(
                label = stringResource(R.string.showcase_tab_all),
                live = selected == null,
                onClick = { onPick(null) },
            )
        }
        items(CreationCategory.browseOrder, key = { it.name }) { category ->
            TypeTab(
                label = creationCategoryLabel(category),
                live = selected == category,
                onClick = { onPick(category) },
            )
        }
    }
}

@Composable
private fun TypeTab(
    label: String,
    live: Boolean,
    onClick: () -> Unit,
) {
    Column(
        // The row scrolls, so its items are measured against an unbounded width — the bar below
        // has to be told to match the word, or it ends up with no width at all.
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .tappable(pressedScale = 0.94f, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label.uppercase(),
            color = if (live) Palette.Accent else Palette.TextFaint,
            fontSize = 12.sp,
            letterSpacing = 1.1.sp,
            fontWeight = if (live) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(vertical = 9.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(if (live) Palette.Accent else Color.Transparent),
        )
    }
}

// endregion

// region the order panel

/** The orders, one line each, the chosen one carrying the torch. */
@Composable
private fun OrderPanel(
    current: CreationFeed,
    onPick: (CreationFeed) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = SIDE_PADDING)
            .padding(bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.showcase_filter_order).uppercase(),
            color = Palette.TextFaint,
            fontSize = 11.sp,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SORTS.forEach { sort ->
            val live = sort == current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KeyShape)
                    .tappable(onClick = { onPick(sort) })
                    .background(if (live) Palette.Accent.copy(alpha = 0.12f) else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 18.dp)
                        .background(if (live) Palette.Accent else Color.Transparent),
                )
                Text(
                    text = stringResource(sort.titleRes()),
                    color = if (live) Palette.TextPrimary else Palette.TextMuted,
                    fontSize = 16.sp,
                    fontWeight = if (live) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                if (live) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Palette.Accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

// endregion

// region list

private fun LazyListScope.catalog(
    state: ShowcaseStore.State,
    nativeAdInterval: Int,
    onOpenCreation: (Int) -> Unit,
) {
    val cadence = AdCadence.of(nativeAdInterval)
    state.items.forEachIndexed { index, creation ->
        item(key = "mod_${creation.id}") {
            CreationCard(
                creation = creation,
                onClick = { onOpenCreation(creation.id) },
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
            )
        }
        if (AdCadence.breaksAfter(index, cadence)) {
            item(key = "ad_$index") {
                NativeSlot(
                    placement = AdPlacement.CATALOG_LIST_NATIVE,
                    slotKey = "catalog_$index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SIDE_PADDING),
                )
            }
        }
    }
}

/** Whatever the foot of the list has to say: a page loading, a retry, an error, or nothing. */
@Composable
private fun Footer(
    state: ShowcaseStore.State,
    onIntent: (Intent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE_PADDING),
        contentAlignment = Alignment.Center,
    ) {
        val stage = state.stage
        when {
            stage is ScreenStage.Failed && state.items.isEmpty() -> ErrorState(
                message = stage.message,
                onRetry = { onIntent(Intent.Refresh) },
            )

            stage.isFailed -> PillButton(
                text = stringResource(R.string.showcase_browse_retry),
                onClick = { onIntent(Intent.LoadMore) },
            )

            stage.isLoading && state.items.isNotEmpty() -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = Palette.Accent,
            )

            state.items.isEmpty() && !stage.isLoading -> EmptyState()

            else -> Box(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PagingTrigger(
    listState: LazyListState,
    state: ShowcaseStore.State,
    onIntent: (Intent) -> Unit,
) {
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= info.totalItemsCount - PRELOAD_DISTANCE
        }
    }
    LaunchedEffect(nearEnd, state.stage, state.endReached, state.items.size) {
        if (nearEnd && !state.stage.isLoading && !state.stage.isFailed && !state.endReached) {
            onIntent(Intent.LoadMore)
        }
    }
}

// endregion

internal fun CreationFeed.titleRes(): Int = when (this) {
    CreationFeed.Trending -> R.string.showcase_section_trending
    CreationFeed.Popular -> R.string.showcase_section_popular
    CreationFeed.TopRated -> R.string.showcase_section_top_rated
    CreationFeed.Fresh -> R.string.showcase_section_fresh
}

private fun categoryLabelRes(category: CreationCategory): Int = when (category) {
    CreationCategory.Addon -> R.string.category_addon
    CreationCategory.Maps -> R.string.category_maps
    CreationCategory.Texture -> R.string.category_texture
    CreationCategory.Skin -> R.string.category_skin
}
