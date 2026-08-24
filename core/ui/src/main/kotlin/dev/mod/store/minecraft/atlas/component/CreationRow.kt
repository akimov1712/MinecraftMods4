package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity

private val ROW_SHAPE = RoundedCornerShape(22.dp)

/**
 * A chart entry: rank numeral, square thumbnail, title and counters, plus whatever [trailing]
 * decoration the section wants (a movement arrow, a chevron…).
 */
@Composable
fun CreationRow(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    rank: Int? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ROW_SHAPE)
            .background(Palette.Surface)
            .border(1.dp, Palette.GlassStroke, ROW_SHAPE)
            .tappable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (rank != null) {
            Text(
                text = rank.toString(),
                modifier = Modifier.width(26.dp),
                color = if (rank <= 3) Palette.Gold else Palette.TextMuted,
                fontSize = if (rank <= 3) 22.sp else 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Palette.SurfaceHigh),
        ) {
            RemoteImage(url = creation.imageUrl, modifier = Modifier.size(58.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CategoryChip(creation.category)
                if (creation.rating > 0.0) {
                    MetaChip(
                        text = formatRating(creation.rating),
                        icon = Icons.Rounded.Star,
                        tint = Palette.Gold,
                    )
                }
                if (creation.reactionCount > 0) {
                    MetaChip(
                        text = formatCompact(creation.reactionCount),
                        icon = Icons.Rounded.LocalFireDepartment,
                        tint = Palette.Ember,
                    )
                } else if (creation.commentCount > 0) {
                    MetaChip(
                        text = formatCompact(creation.commentCount),
                        icon = Icons.Rounded.ChatBubble,
                        tint = Palette.Sky,
                    )
                }
            }
        }

        trailing?.invoke(this)
    }
}
