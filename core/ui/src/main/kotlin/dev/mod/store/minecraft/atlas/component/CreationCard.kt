package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity

private val CARD_SHAPE = RoundedCornerShape(30.dp)

/**
 * The full-width browse card: cover art under a deep scrim, the category and save state pinned
 * to the top, and the title with its counters resting on the bottom edge.
 */
@Composable
fun CreationCard(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .clip(CARD_SHAPE)
            .background(Palette.Surface)
            .tappable(onClick = onClick),
    ) {
        RemoteImage(
            url = creation.imageUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        ScrimGradient()

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryChip(creation.category)
            if (creation.fileUrls.size > 1) {
                MetaChip(
                    text = creation.fileUrls.size.toString(),
                    icon = Icons.Rounded.Download,
                    tint = Palette.TextPrimary,
                    container = Palette.Scrim,
                )
            }
            Box(Modifier.weight(1f))
            if (creation.isBookmarked) BookmarkBadge()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.OnAccent,
                fontSize = 19.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (creation.rating > 0.0) {
                    MetaChip(
                        text = formatRating(creation.rating),
                        icon = Icons.Rounded.Star,
                        tint = Palette.Gold,
                        container = Palette.Scrim,
                    )
                }
                if (creation.reactionCount > 0) {
                    MetaChip(
                        text = formatCompact(creation.reactionCount),
                        icon = Icons.Rounded.LocalFireDepartment,
                        tint = Palette.Ember,
                        container = Palette.Scrim,
                    )
                }
                if (creation.commentCount > 0) {
                    MetaChip(
                        text = formatCompact(creation.commentCount),
                        icon = Icons.Rounded.ChatBubble,
                        tint = Palette.Sky,
                        container = Palette.Scrim,
                    )
                }
                creation.supportedVersions.firstOrNull()?.let { version ->
                    MetaChip(text = version, tint = Palette.TextMuted, container = Palette.Scrim)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Palette.GlassStroke, CARD_SHAPE),
        )
    }
}

@Composable
private fun BoxScope.ScrimGradient() {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Palette.Canvas.copy(alpha = 0.55f),
                        Palette.Canvas.copy(alpha = 0.94f),
                    ),
                ),
            ),
    )
}

@Composable
private fun BookmarkBadge() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Palette.Accent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Bookmark,
            contentDescription = null,
            tint = Palette.OnAccentDark,
            modifier = Modifier.size(17.dp),
        )
    }
}
