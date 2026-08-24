package dev.mod.store.minecraft.feature.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.RefreshSurface
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.feature.showcase.ShowcaseStore.Intent

/**
 * Home: the editorial digest and the "one section in full" list live in the same scrollable feed,
 * wrapped in pull-to-refresh. The only thing this level owns is the snackbar.
 */
@Composable
fun ShowcasePane(
    component: ShowcaseComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    ObserveSignals(component.labels) { label ->
        when (label) {
            is ShowcaseStore.Label.Warn -> snackbar.showSnackbar(label.message)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        RefreshSurface(
            refreshing = state.refreshing,
            onRefresh = { component.onIntent(Intent.Refresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            HomeFeed(
                state = state,
                nativeAdInterval = component.nativeAdInterval,
                onIntent = component::onIntent,
                onOpenCreation = component::openCreation,
                modifier = Modifier.fillMaxSize(),
            )
        }

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}
