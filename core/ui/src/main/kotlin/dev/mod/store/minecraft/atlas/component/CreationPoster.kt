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
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity

/**
 * The rail tile: artwork, name and one line of metadata. Deliberately narrow — three of them
 * should hint at the fourth off the right edge.
 */
@Composable
fun CreationPoster(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 184.dp,
    aspectRatio: Float = 1f,
    rank: Int? = null,
    accent: Color = Palette.Accent,
) {
    Column(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .tappable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(SmallShape)
                .background(Palette.SurfaceHigh),
        ) {
            RemoteImage(
                url = creation.imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            if (rank != null) {
                RankMedal(
                    rank = rank,
                    accent = accent,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                )
            }
            if (creation.isBookmarked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .popIn()
                        .size(26.dp)
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

        Text(
            text = creation.title,
            color = Palette.TextPrimary,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            creation.supportedVersions.firstOrNull()?.let { version ->
                Text(
                    text = "v$version+",
                    color = Palette.TextFaint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
            if (creation.rating > 0.0) {
                MetaChip(
                    text = formatRating(creation.rating),
                    icon = Icons.Rounded.Star,
                    tint = Palette.Gold,
                )
            }
        }
    }
}

/** The place number worn by ranked tiles. */
@Composable
fun RankMedal(
    rank: Int,
    modifier: Modifier = Modifier,
    accent: Color = Palette.Accent,
) {
    Text(
        text = rank.toString(),
        color = Palette.OnAccentDark,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .popIn()
            .clip(SmallShape)
            .background(accent)
            .padding(horizontal = 9.dp, vertical = 3.dp),
    )
}
