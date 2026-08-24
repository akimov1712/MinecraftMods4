package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.modifier.pressable
import dev.mod.store.minecraft.core.ui.theme.Palette

/** A horizontally-scrollable row of pill segments with a single active selection. */
@Composable
fun SegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    spacing: Dp = 10.dp,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        itemsIndexed(labels) { index, label ->
            Segment(
                label = label,
                active = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun Segment(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val textColor = if (active) Palette.OnAccentDark else Palette.TextMuted

    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(if (active) Palette.AccentGradient else SolidColor(Palette.Surface))
            .then(
                if (active) Modifier else Modifier.border(1.dp, Palette.Stroke, CircleShape),
            )
            .pressable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
