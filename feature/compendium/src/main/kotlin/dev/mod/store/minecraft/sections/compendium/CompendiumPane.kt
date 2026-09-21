package dev.mod.store.minecraft.feature.compendium

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ads.NativeSlot
import dev.mod.store.minecraft.core.ui.R
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
 * Help, played out as a dialogue: the assistant opens, the reader picks a question, and the answer
 * arrives as the next message.
 *
 * The board of questions starts open, so everything on offer is readable the moment the screen
 * appears rather than hidden off the side of a scrolling strip. Asking the first one folds it down
 * to a single line and hands the room to the conversation; that line opens the board again whenever
 * it is wanted.
 */
@Composable
fun CompendiumPane(
    component: CompendiumComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var boardOpen by rememberSaveable { mutableStateOf(true) }

    val lines = remember(state.asked) {
        buildList {
            state.asked.forEach { id ->
                // An id can outlive its question across an update; skip it rather than crash.
                val entry = faqEntries.firstOrNull { it.id == id } ?: return@forEach
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
                // The conversation is over and the question board is gone: the only point on this
                // screen where an ad is not standing between a reader and an answer.
                if (component.hasNativeAd) {
                    item(key = "ad") {
                        NativeSlot(
                            slotKey = "compendium",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                        )
                    }
                }
            }
        }

        QuestionBoard(
            entries = state.remaining,
            open = boardOpen,
            onToggle = { boardOpen = !boardOpen },
            onAsk = { id ->
                // The first answer is what the reader came for; give it the screen.
                boardOpen = false
                component.onIntent(Intent.Ask(id))
            },
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

private val BOARD_SHAPE = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)

/** How much of the screen the open board may take before it starts scrolling on its own. */
private const val BOARD_MAX_FRACTION = 0.46f

/**
 * Everything the assistant can answer, laid out in full. Open, the questions wrap across the panel
 * so the whole menu is read at once; closed, it is one line stating how many are left.
 */
@Composable
private fun QuestionBoard(
    entries: List<FaqEntry>,
    open: Boolean,
    onToggle: () -> Unit,
    onAsk: (String) -> Unit,
) {
    if (entries.isEmpty()) return

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val boardMax = maxHeight * BOARD_MAX_FRACTION

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BOARD_SHAPE)
                .background(Palette.Surface)
                .border(1.dp, Palette.Stroke, BOARD_SHAPE)
                .animateContentSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tappable(pressedScale = 0.99f, onClick = onToggle)
                    .padding(horizontal = SIDE_PADDING, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.QuestionAnswer,
                    contentDescription = null,
                    tint = Palette.AccentSoft,
                    modifier = Modifier.size(19.dp),
                )
                Text(
                    text = if (open) {
                        stringResource(R.string.compendium_prompt)
                    } else {
                        stringResource(R.string.compendium_show_all, entries.size)
                    },
                    color = Palette.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (open) {
                        Icons.Rounded.KeyboardArrowDown
                    } else {
                        Icons.Rounded.KeyboardArrowUp
                    },
                    contentDescription = stringResource(
                        if (open) R.string.compendium_collapse else R.string.compendium_expand,
                    ),
                    tint = Palette.TextFaint,
                    modifier = Modifier.size(24.dp),
                )
            }

            if (open) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = boardMax)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = SIDE_PADDING)
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    entries.forEach { entry ->
                        Text(
                            text = stringResource(entry.chipRes),
                            color = Palette.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(SmallShape)
                                .background(Palette.SurfaceHigh)
                                .border(1.dp, Palette.Stroke, SmallShape)
                                .tappable { onAsk(entry.id) }
                                .padding(horizontal = 13.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}
