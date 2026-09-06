package dev.mod.store.minecraft.feature.compendium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.component.GlassIconButton
import dev.mod.store.minecraft.core.ui.effect.Appear
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.feature.compendium.CompendiumStore.Intent

private val SIDE_PADDING = 16.dp

/** One line of the conversation. */
private data class Line(val text: String, val fromUser: Boolean, val key: String)

/**
 * Help, played out as a dialogue: the assistant opens, the reader picks a question from the row
 * at the bottom, and the answer arrives as the next message. Nothing folds open and nothing is
 * hidden behind a chevron — the whole session stays on screen and can be replayed from scratch.
 */
@Composable
fun CompendiumPane(
    component: CompendiumComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val lines = remember(state.asked) {
        buildList {
            state.asked.forEach { id ->
                val entry = faqEntries.first { it.id == id }
                add(Line(context.getString(entry.questionRes), fromUser = true, key = "q_$id"))
                add(Line(context.getString(entry.answerRes), fromUser = false, key = "a_$id"))
            }
        }
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Header(
            canRestart = state.asked.isNotEmpty(),
            onRestart = { component.onIntent(Intent.Restart) },
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = SIDE_PADDING, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "greeting") {
                Bubble(
                    text = stringResource(R.string.compendium_greeting),
                    fromUser = false,
                )
            }
            items(items = lines, key = { it.key }) { line ->
                Appear {
                    Bubble(text = line.text, fromUser = line.fromUser)
                }
            }
            if (state.remaining.isEmpty()) {
                item(key = "done") {
                    Bubble(
                        text = stringResource(R.string.compendium_done),
                        fromUser = false,
                    )
                }
            }
        }

        QuestionRow(
            entries = state.remaining,
            onAsk = { component.onIntent(Intent.Ask(it)) },
        )
    }
}

@Composable
private fun Header(canRestart: Boolean, onRestart: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SIDE_PADDING, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Palette.Accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.SupportAgent,
                contentDescription = null,
                tint = Palette.Accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.compendium_title),
                color = Palette.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.compendium_subtitle),
                color = Palette.TextFaint,
                fontSize = 12.sp,
            )
        }
        if (canRestart) {
            GlassIconButton(
                icon = Icons.Rounded.Refresh,
                contentDescription = stringResource(R.string.compendium_restart),
                onClick = onRestart,
                size = 36.dp,
            )
        }
    }
}

/** A message. The assistant speaks from the left, the reader from the right. */
@Composable
private fun Bubble(text: String, fromUser: Boolean) {
    val shape = if (fromUser) {
        RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Text(
            text = text,
            color = if (fromUser) Palette.OnAccentDark else Palette.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(if (fromUser) Palette.Accent else Palette.Surface)
                .padding(horizontal = 13.dp, vertical = 10.dp),
        )
    }
}

/** The questions still on the table, as a scrollable row of prompts. */
@Composable
private fun QuestionRow(entries: List<FaqEntry>, onAsk: (String) -> Unit) {
    if (entries.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Palette.Canvas)
            .padding(bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.compendium_prompt),
            color = Palette.TextFaint,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = SIDE_PADDING),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = SIDE_PADDING),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(items = entries, key = { it.id }) { entry ->
                Text(
                    text = stringResource(entry.chipRes),
                    color = Palette.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(SmallShape)
                        .background(Palette.SurfaceHigh)
                        .tappable { onAsk(entry.id) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                )
            }
        }
    }
}
