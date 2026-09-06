package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity

/** A chart line: place, small cover, name and its numbers on one line. */
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
            .clip(SmallShape)
            .tappable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (rank != null) {
            Text(
                text = rank.toString(),
                modifier = Modifier.width(26.dp),
                color = if (rank <= 3) Palette.Accent else Palette.TextFaint,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        trailing?.invoke(this)

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(SmallShape)
                .background(Palette.SurfaceHigh),
        ) {
            RemoteImage(url = creation.imageUrl, modifier = Modifier.size(64.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = creationCategoryLabel(creation.category),
                    color = Palette.TextFaint,
                    fontSize = 13.sp,
                )
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
                }
            }
        }
    }
}
