package dev.mod.store.minecraft.feature.showcase

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.mod.store.minecraft.core.ads.AdCadence
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.CreationCard
import dev.mod.store.minecraft.core.ui.component.CreationCardSkeleton
import dev.mod.store.minecraft.core.ui.component.EmptyState
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.creationCategoryAccent
import dev.mod.store.minecraft.core.ui.component.creationCategoryIcon
import dev.mod.store.minecraft.core.ui.component.creationCategoryLabel
import dev.mod.store.minecraft.core.ui.effect.CardShape
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

private const val SKELETON_CARDS = 4

private val SHEET_SHAPE = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)

/** Content kinds in the order the tab strip shows them: the two anyone comes for lead. */
private val KINDS = listOf(
    CreationCategory.Addon,
    CreationCategory.Maps,
    CreationCategory.Texture,
    CreationCategory.Skin,
)

/** The orders offered, in the order the sheet lists them. */
private val SORTS = listOf(
    CreationFeed.Trending,
    CreationFeed.Popular,
    CreationFeed.Fresh,
    CreationFeed.TopRated,
)

/**
 * Home: one list of the catalog, and one key that opens the sheet deciding what is in it.
 *
 * The page itself carries no controls — no tab strip, no menu, no counter. Everything that shapes
 * the list lives in a sheet that rises from the foot of the screen with targets big enough to hit
 * without aiming, and what is currently chosen is written under the title in plain words. Picking
 * in the sheet takes effect at once, so the list is already right when the sheet goes away.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFeed(
    state: ShowcaseStore.State,
    nativeAdInterval: Int,
    onIntent: (Intent) -> Unit,
    onOpenCreation: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var sheetOpen by remember { mutableStateOf(false) }

    PagingTrigger(listState, state, onIntent)

    // A new slice is a new list: start it from the top rather than halfway down the old one.
    LaunchedEffect(state.category, state.sort) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        stickyHeader(key = "header") {
            Header(
                state = state,
                onOpenSort = { sheetOpen = true },
                onPickCategory = { onIntent(Intent.ChangeCategory(it)) },
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

    if (sheetOpen) {
        SortSheet(
            state = state,
            onIntent = onIntent,
            onClose = { sheetOpen = false },
        )
    }
}

// region header

/** The title, the key that opens the order window, and the tab strip of content kinds. */
@Composable
private fun Header(
    state: ShowcaseStore.State,
    onOpenSort: () -> Unit,
    onPickCategory: (CreationCategory?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.Canvas)
            .statusBarsPadding()
            .padding(top = 10.dp, bottom = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.hub_tab_showcase),
                color = Palette.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            // Lit while the order is not the house one, so a surprising list has a visible reason.
            val sorted = state.sort != CreationFeed.Trending
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CardShape)
                    .tappable(onClick = onOpenSort)
                    .background(if (sorted) Palette.Accent else Palette.Surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.showcase_filter_order),
                    tint = if (sorted) Palette.OnAccentDark else Palette.TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = SIDE_PADDING),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "all") {
                KindTab(
                    label = stringResource(R.string.showcase_filter_all),
                    selected = state.category == null,
                    onClick = { onPickCategory(null) },
                )
            }
            items(KINDS, key = { it.name }) { category ->
                KindTab(
                    label = creationCategoryLabel(category),
                    selected = state.category == category,
                    onClick = { onPickCategory(category) },
                )
            }
        }
    }
}

/** One tab: the name, and a short bar under the one in force. */
@Composable
private fun KindTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .tappable(onClick = onClick)
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = if (selected) Palette.TextPrimary else Palette.TextFaint,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(if (selected) 20.dp else 0.dp)
                .clip(CircleShape)
                .background(Palette.Accent),
        )
    }
}

// endregion

// region sheet

/**
 * The order, in a window of its own: rows that say in plain words what each one does. Nothing is
 * staged — a tap applies at once, and the list behind is already rebuilt when the window closes.
 */
@Composable
private fun SortSheet(
    state: ShowcaseStore.State,
    onIntent: (Intent) -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .tappable(pressedScale = 1f, onClick = onClose),
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
                    .padding(horizontal = SIDE_PADDING, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Palette.Stroke),
                )

                Text(
                    text = stringResource(R.string.showcase_filter_order),
                    color = Palette.TextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SORTS.forEach { sort ->
                        OrderRow(
                            title = stringResource(sort.titleRes()),
                            note = stringResource(sort.noteRes()),
                            selected = state.sort == sort,
                            onClick = { onIntent(Intent.ChangeSort(sort)) },
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                PillButton(
                    text = stringResource(R.string.showcase_filter_apply),
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Palette.TextFaint,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
    )
}

/** One order, with the plain-words version of what it does under its name. */
@Composable
private fun OrderRow(
    title: String,
    note: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmallShape)
            .tappable(onClick = onClick)
            .background(if (selected) Palette.SurfaceHigh else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Palette.TextPrimary,
                fontSize = 16.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            Text(
                text = note,
                color = Palette.TextFaint,
                fontSize = 13.sp,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) Palette.Accent else Color.Transparent)
                .border(
                    width = if (selected) 0.dp else 1.5.dp,
                    color = if (selected) Color.Transparent else Palette.Stroke,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Palette.OnAccentDark,
                    modifier = Modifier.size(15.dp),
                )
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

/** The same order said plainly, for readers who do not sort catalogues for a living. */
private fun CreationFeed.noteRes(): Int = when (this) {
    CreationFeed.Trending -> R.string.showcase_sort_trending_note
    CreationFeed.Popular -> R.string.showcase_sort_popular_note
    CreationFeed.TopRated -> R.string.showcase_sort_rated_note
    CreationFeed.Fresh -> R.string.showcase_sort_fresh_note
}
