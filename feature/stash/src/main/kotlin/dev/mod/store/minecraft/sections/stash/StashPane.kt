package dev.mod.store.minecraft.feature.stash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.component.CreationCard
import dev.mod.store.minecraft.core.ui.component.EmptyState
import dev.mod.store.minecraft.core.ui.component.PagedColumn
import dev.mod.store.minecraft.core.ui.component.RefreshSurface
import dev.mod.store.minecraft.core.ui.component.ShimmerBox
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.stash.StashStore.Intent

@Composable
fun StashPane(
    component: StashComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    Column(modifier = modifier.fillMaxSize().padding(top = 12.dp)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.stash_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Palette.TextPrimary,
            )
            Text(
                text = stringResource(R.string.stash_count, state.count),
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.TextMuted,
            )
        }

        RefreshSurface(
            refreshing = false,
            onRefresh = { component.onIntent(Intent.Refresh) },
            modifier = Modifier.fillMaxSize(),
        ) {
            PagedColumn(
                items = state.creations,
                stage = state.stage,
                endReached = state.endReached,
                onLoadMore = { component.onIntent(Intent.LoadMore) },
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                skeleton = {
                    items(3) {
                        ShimmerBox(
                            modifier = Modifier.fillMaxWidth().aspectRatio(1.1f),
                            shape = RoundedCornerShape(28.dp),
                        )
                    }
                },
                empty = {
                    item {
                        EmptyState(
                            title = stringResource(R.string.stash_empty),
                            message = stringResource(R.string.stash_empty_hint),
                        )
                    }
                },
            ) { creations ->
                items(items = creations, key = { it.id }) { creation ->
                    CreationCard(
                        creation = creation,
                        onClick = { component.openCreation(creation.id) },
                    )
                }
            }
        }
    }
}
