package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity

/**
 * The list row used by search results and "see all" lists: a wide cover on the left, the name
 * and its numbers on the right. Landscape rather than a full-width poster, so several fit on a
 * screen at once.
 */
@Composable
fun CreationCard(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(SmallShape)
            .tappable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(150.dp)
                .aspectRatio(1.35f)
                .clip(SmallShape)
                .background(Palette.SurfaceHigh),
        ) {
            RemoteImage(
                url = creation.imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (creation.isBookmarked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .popIn()
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Palette.Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = null,
                        tint = Palette.OnAccentDark,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            CategoryChip(creation.category)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                creation.supportedVersions.firstOrNull()?.let { version ->
                    MetaChip(text = "v$version+", tint = Palette.TextFaint)
                }
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
    }
}
