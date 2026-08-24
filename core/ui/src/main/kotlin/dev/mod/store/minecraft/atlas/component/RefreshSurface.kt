package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.mod.store.minecraft.core.ui.theme.Palette

/** Wraps scrollable content with a themed pull-to-refresh gesture and indicator. */
@Composable
fun RefreshSurface(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = refreshing,
        state = state,
        onRefresh = onRefresh,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = refreshing,
                state = state,
                containerColor = Palette.Surface,
                color = Palette.Accent,
            )
        },
        content = content,
    )
}
