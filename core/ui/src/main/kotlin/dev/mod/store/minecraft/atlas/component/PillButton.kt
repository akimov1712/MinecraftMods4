package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.effect.CardBox
import androidx.compose.foundation.shape.CircleShape
import dev.mod.store.minecraft.core.ui.theme.Palette

/** A big, obvious button. Tall enough for small fingers, with a plain sentence-case label. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    busy: Boolean = false,
    container: Color = Palette.Accent,
    content: Color = Palette.OnAccentDark,
    leading: @Composable (() -> Unit)? = null,
) {
    CardBox(
        modifier = modifier.defaultMinSize(minHeight = 56.dp),
        fill = if (enabled) container else container.copy(alpha = 0.4f),
        shape = CircleShape,
        enabled = enabled && !busy,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp),
                color = content,
                strokeWidth = 2.5.dp,
            )
        } else {
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading?.invoke()
                Text(
                    text = text,
                    color = content,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
