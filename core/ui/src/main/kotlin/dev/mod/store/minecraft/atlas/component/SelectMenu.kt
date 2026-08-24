package dev.mod.store.minecraft.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.modifier.pressable
import dev.mod.store.minecraft.core.ui.theme.Palette

/** A compact anchor + dropdown for picking one option out of [options]. */
@Composable
fun SelectMenu(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val label = options.getOrNull(selectedIndex).orEmpty()
    val chevron by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        animationSpec = tween(200),
        label = "chevron",
    )

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(CircleShape)
                .background(Palette.Surface)
                .border(1.dp, Palette.Stroke, CircleShape)
                .pressable { open = !open }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = Palette.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = Palette.TextMuted,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(chevron),
            )
        }

        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            containerColor = Palette.SurfaceHigh,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Palette.Stroke),
        ) {
            options.forEachIndexed { index, option ->
                val active = index == selectedIndex
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (active) Palette.Accent else Palette.TextPrimary,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(index)
                        open = false
                    },
                    colors = MenuDefaults.itemColors(textColor = Palette.TextPrimary),
                )
            }
        }
    }
}
