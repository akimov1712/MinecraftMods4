package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.domain.creation.CreationCategory

private val CHIP_SHAPE = RoundedCornerShape(8.dp)

/** A compact readout: a small icon and a value. */
@Composable
fun MetaChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = Palette.TextMuted,
    container: Color = Color.Transparent,
    outlined: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(CHIP_SHAPE)
            .background(container)
            .padding(horizontal = if (container == Color.Transparent) 0.dp else 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(
            text = text,
            color = tint,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

/** Category tag: a dot of the category's colour and its name. */
@Composable
fun CategoryChip(
    category: CreationCategory,
    modifier: Modifier = Modifier,
) {
    val accent = creationCategoryAccent(category)
    Row(
        modifier = modifier
            .clip(CHIP_SHAPE)
            .background(accent.copy(alpha = 0.16f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = creationCategoryIcon(category),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = creationCategoryLabel(category),
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}
