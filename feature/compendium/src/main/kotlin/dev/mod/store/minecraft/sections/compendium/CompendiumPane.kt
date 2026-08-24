package dev.mod.store.minecraft.feature.compendium

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.rounded.Search
import dev.mod.store.minecraft.core.ui.component.EmptyState
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.modifier.pressable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.compendium.CompendiumStore.Intent

@Composable
fun CompendiumPane(
    component: CompendiumComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    val query = state.query.trim()

    val visibleGroups = remember(query) {
        faqGroups.mapNotNull { group ->
            val entries = group.entries.filter { entry ->
                query.isBlank() ||
                    context.getString(entry.questionRes).contains(query, ignoreCase = true) ||
                    context.getString(entry.answerRes).contains(query, ignoreCase = true)
            }
            if (entries.isEmpty()) null else group to entries
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(top = 12.dp)) {
        Text(
            text = stringResource(R.string.compendium_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Palette.TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        OutlineField(
            value = state.query,
            onValueChange = { component.onIntent(Intent.Search(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = stringResource(R.string.compendium_search_hint),
            leading = Icons.Rounded.Search,
        )

        if (visibleGroups.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.compendium_empty),
                modifier = Modifier.padding(16.dp),
            )
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            visibleGroups.forEach { (group, entries) ->
                item(key = "group_${group.titleRes}") {
                    Text(
                        text = stringResource(group.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        color = Palette.AccentSoft,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }
                items(items = entries, key = { it.id }) { entry ->
                    FaqRow(
                        question = stringResource(entry.questionRes),
                        answer = stringResource(entry.answerRes),
                        expanded = state.expandedId == entry.id,
                        onToggle = { component.onIntent(Intent.ToggleEntry(entry.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FaqRow(
    question: String,
    answer: String,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Palette.Surface)
            .pressable(onClick = onToggle)
            .padding(16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Palette.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = Palette.TextMuted,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(if (expanded) 180f else 0f),
            )
        }
        if (expanded) {
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.TextMuted,
            )
        }
    }
}
