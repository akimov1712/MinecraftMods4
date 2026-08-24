package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.state.ScreenStage
import dev.mod.store.minecraft.core.ui.theme.Palette

private const val PRELOAD_DISTANCE = 3

/**
 * A LazyColumn wired for infinite scroll. It shows a [skeleton] while the first page loads,
 * a footer spinner between pages, an inline/full error on [ScreenStage.Failed], and [empty]
 * once a successful load yields nothing. [onLoadMore] fires as the user nears the end.
 */
@Composable
fun <T> PagedColumn(
    items: List<T>,
    stage: ScreenStage,
    endReached: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    skeleton: LazyListScope.() -> Unit = {},
    empty: (LazyListScope.() -> Unit)? = null,
    content: LazyListScope.(items: List<T>) -> Unit,
) {
    PreloadTrigger(state, stage, endReached, onLoadMore)

    LazyColumn(
        state = state,
        modifier = modifier,
        userScrollEnabled = items.isNotEmpty() || !stage.isLoading,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        contentPadding = contentPadding,
    ) {
        if (items.isEmpty() && stage.isLoading) {
            skeleton()
        } else {
            content(items)
        }
        footer(stage, endReached, items.isEmpty(), empty, onLoadMore)
    }
}

private fun LazyListScope.footer(
    stage: ScreenStage,
    endReached: Boolean,
    isEmpty: Boolean,
    empty: (LazyListScope.() -> Unit)?,
    onLoadMore: () -> Unit,
) {
    when (stage) {
        is ScreenStage.Failed -> item {
            if (isEmpty) {
                ErrorState(message = stage.message, onRetry = onLoadMore)
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    PillButton(text = retryLabel(), onClick = onLoadMore)
                }
            }
        }

        ScreenStage.Loading -> if (!endReached) {
            item { FooterSpinner() }
        }

        ScreenStage.Ready -> if (isEmpty) {
            if (empty != null) empty() else item { EmptyState() }
        }

        ScreenStage.Idle -> Unit
    }
}

@Composable
private fun FooterSpinner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.5.dp,
            color = Palette.Accent,
        )
    }
}

@Composable
private fun retryLabel(): String =
    androidx.compose.ui.res.stringResource(dev.mod.store.minecraft.core.ui.R.string.atlas_retry)

@Composable
private fun PreloadTrigger(
    state: LazyListState,
    stage: ScreenStage,
    endReached: Boolean,
    onLoadMore: () -> Unit,
) {
    val shouldLoadMore = remember(state) {
        derivedStateOf {
            val info = state.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            lastVisible >= info.totalItemsCount - PRELOAD_DISTANCE
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !stage.isLoading && !stage.isFailed && !endReached) {
            onLoadMore()
        }
    }
}
