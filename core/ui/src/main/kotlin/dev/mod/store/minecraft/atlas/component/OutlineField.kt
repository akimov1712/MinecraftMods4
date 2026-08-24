package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.modifier.pressable
import dev.mod.store.minecraft.core.ui.theme.Palette

/**
 * Bordered input. Renders as a circular pill for single-line use and a rounded card when it
 * grows to multiple lines. Icons are optional; the trailing one can be tapped.
 */
@Composable
fun OutlineField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leading: ImageVector? = null,
    trailing: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    minHeight: Dp = 52.dp,
    horizontalPadding: Dp = 20.dp,
) {
    val multiline = !singleLine || minLines > 1 || maxLines > 1
    val shape: Shape = if (multiline) RoundedCornerShape(20.dp) else CircleShape
    val verticalPadding = if (multiline) 14.dp else 0.dp
    val rowAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .clip(shape)
            .background(Palette.Surface)
            .border(width = 1.dp, color = Palette.Stroke, shape = shape)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = rowAlignment,
    ) {
        if (leading != null) {
            Icon(
                imageVector = leading,
                contentDescription = null,
                tint = Palette.TextMuted,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
        }
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    text = placeholder,
                    color = Palette.TextMuted.copy(alpha = 0.7f),
                    style = LocalTextStyle.current,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                minLines = minLines,
                maxLines = maxLines,
                enabled = enabled,
                keyboardOptions = keyboardOptions,
                textStyle = LocalTextStyle.current.copy(color = Palette.TextPrimary),
                cursorBrush = SolidColor(Palette.Accent),
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            val iconModifier = Modifier
                .size(20.dp)
                .let { if (onTrailingClick != null) it.pressable(onClick = onTrailingClick) else it }
            Icon(
                imageVector = trailing,
                contentDescription = null,
                tint = Palette.TextMuted,
                modifier = iconModifier,
            )
        }
    }
}
