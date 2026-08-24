package dev.mod.store.minecraft.feature.search

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.component.CreationCard
import dev.mod.store.minecraft.core.ui.component.CreationPoster
import dev.mod.store.minecraft.core.ui.component.EmptyState
import dev.mod.store.minecraft.core.ui.component.ErrorState
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.component.SectionHeader
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.effect.Appear
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.core.ui.util.formatCount
import dev.mod.store.minecraft.feature.showcase.R
import dev.mod.store.minecraft.feature.search.SearchStore.Intent

private const val PRELOAD_DISTANCE = 3
private val SIDE_PADDING = 16.dp

/**
 * The search screen. The field unfolds from the round button that opened it, takes focus on its
 * own, and until something is typed the screen keeps offering the editorial sections instead of
 * an empty page.
 */
@Composable
fun SearchPane(
    component: SearchComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    val unfold by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "search-unfold",
    )
    val fade by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(320, easing = EaseOutCubic),
        label = "search-fade",
    )

    ObserveSignals(component.labels) { label ->
        when (label) {
            is SearchStore.Label.Warn -> snackbar.showSnackbar(label.message)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    PagingTrigger(listState, state, component::onIntent)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = SIDE_PADDING, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.search_back),
                    onClick = component::back,
                )
                OutlineField(
                    value = state.query,
                    onValueChange = { component.onIntent(Intent.SetQuery(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            // Unfolds from the round button that opened the screen.
                            scaleX = 0.25f + 0.75f * unfold
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                            alpha = fade
                        }
                        .focusRequester(focusRequester),
                    placeholder = stringResource(R.string.search_hint),
                    leading = Icons.Rounded.Search,
                    trailing = if (state.query.isNotEmpty()) Icons.Rounded.Close else null,
                    onTrailingClick = { component.onIntent(Intent.Clear) },
                    minHeight = 52.dp,
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = fade },
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (state.query.isBlank()) {
                    suggestions(state, component::openCreation)
                } else {
                    results(state, component.nativeAdInterval, component::onIntent, component::openCreation)
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
private fun PagingTrigger(
    listState: LazyListState,
    state: SearchStore.State,
    onIntent: (Intent) -> Unit,
) {
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            last >= info.totalItemsCount - PRELOAD_DISTANCE
        }
    }
    LaunchedEffect(nearEnd, state.stage, state.endReached, state.results.size) {
        if (nearEnd && state.query.isNotBlank() && !state.stage.isLoading && !state.stage.isFailed && !state.endReached) {
            onIntent(Intent.LoadMore)
        }
    }
}

/** What the screen shows before anything is typed: the same sections as home. */
private fun LazyListScope.suggestions(
    state: SearchStore.State,
    onOpenCreation: (Int) -> Unit,
) {
    val digest = state.suggestions

    if (digest.isEmpty && !state.suggestionsLoading) {
        item(key = "suggestions_empty") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SIDE_PADDING, vertical = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = Palette.TextFaint,
                    fontSize = 13.sp,
                )
            }
        }
        return
    }

    if (digest.isEmpty && state.suggestionsLoading) {
        item(key = "suggestions_skeleton") {
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
        return
    }

    if (digest.trending.isNotEmpty()) {
        item(key = "suggest_trending") {
            Appear(index = 0) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_trending),
                        subtitle = stringResource(R.string.search_suggestions_hint),
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        accent = Palette.Accent,
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

    if (digest.fresh.isNotEmpty()) {
        item(key = "suggest_fresh") {
            Appear(index = 1) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_fresh),
                        subtitle = stringResource(R.string.showcase_section_fresh_subtitle),
                        icon = Icons.Rounded.NewReleases,
                        accent = Palette.Positive,
                        modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = SIDE_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(digest.fresh, key = { it.id }) { creation ->
                            CreationPoster(
                                creation = creation,
                                onClick = { onOpenCreation(creation.id) },
                                accent = Palette.Positive,
                            )
                        }
                    }
                }
            }
        }
    }

    if (digest.topRated.isNotEmpty()) {
        item(key = "suggest_top") {
            Appear(index = 2) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(
                        title = stringResource(R.string.showcase_section_top_rated),
                        subtitle = stringResource(R.string.showcase_section_top_rated_subtitle),
                        icon = Icons.Rounded.NewReleases,
                        accent = Palette.Sky,
                        modifier = Modifier.padding(horizontal = SIDE_PADDING),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = SIDE_PADDING),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(digest.topRated, key = { it.id }) { creation ->
                            CreationPoster(
                                creation = creation,
                                onClick = { onOpenCreation(creation.id) },
                                accent = Palette.Sky,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.results(
    state: SearchStore.State,
    nativeAdInterval: Int,
    onIntent: (Intent) -> Unit,
    onOpenCreation: (Int) -> Unit,
) {
    if (state.results.isNotEmpty()) {
        item(key = "results_count") {
            Text(
                text = stringResource(R.string.showcase_browse_count, formatCount(state.total)),
                color = Palette.TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
            )
        }
    }

    if (state.results.isEmpty() && state.stage.isLoading) {
        items(3, key = { index -> "result_skeleton_$index" }) {
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

    state.results.forEachIndexed { index, creation ->
        item(key = "result_${creation.id}") {
            CreationCard(
                creation = creation,
                onClick = { onOpenCreation(creation.id) },
                modifier = Modifier.padding(horizontal = SIDE_PADDING),
            )
        }
        if (nativeAdInterval > 0 && (index + 1) % nativeAdInterval == 0) {
            item(key = "result_ad_$index") {
                NativeSlot(
                    slotKey = "search_$index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SIDE_PADDING),
                )
            }
        }
    }

    item(key = "results_footer") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SIDE_PADDING),
            contentAlignment = Alignment.Center,
        ) {
            val stage = state.stage
            when {
                stage is ScreenStage.Failed && state.results.isEmpty() -> ErrorState(
                    message = stage.message,
                    onRetry = { onIntent(Intent.Retry) },
                )

                stage.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Palette.Accent,
                )

                state.results.isEmpty() && stage.isReady -> EmptyState(
                    title = stringResource(R.string.search_empty_title),
                    message = stringResource(R.string.search_empty_body),
                )

                else -> Box(Modifier.height(4.dp))
            }
        }
    }
}
