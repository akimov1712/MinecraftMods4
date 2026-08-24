package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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

/**
 * The small "icon + number" badge that annotates cards (rating, reactions, comments). Sits on a
 * translucent layer so it reads over both artwork and flat surfaces.
 */
@Composable
fun MetaChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = Palette.TextPrimary,
    container: Color = Palette.Glass,
    outlined: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .then(if (outlined) Modifier.border(1.dp, tint.copy(alpha = 0.45f), CircleShape) else Modifier)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
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
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

/** Category name in its own accent colour — the pill every creation card carries. */
@Composable
fun CategoryChip(
    category: dev.mod.store.minecraft.domain.creation.CreationCategory,
    modifier: Modifier = Modifier,
) {
    val accent = creationCategoryAccent(category)
    MetaChip(
        text = creationCategoryLabel(category),
        modifier = modifier,
        icon = creationCategoryIcon(category),
        tint = accent,
        container = accent.copy(alpha = 0.16f),
        outlined = true,
    )
}
