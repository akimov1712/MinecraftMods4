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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.effect.halo
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.domain.creation.CreationEntity

private val POSTER_SHAPE = RoundedCornerShape(24.dp)

/**
 * The portrait card the home rails are made of: artwork under a scrim, an optional [rank] medal,
 * and the title plus its rating/reaction counters stacked at the bottom.
 */
@Composable
fun CreationPoster(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp? = 172.dp,
    aspectRatio: Float = 0.72f,
    rank: Int? = null,
    accent: Color = Palette.Accent,
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .aspectRatio(aspectRatio)
            .clip(POSTER_SHAPE)
            .background(Palette.Surface)
            .tappable(onClick = onClick),
    ) {
        RemoteImage(
            url = creation.imageUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        PosterScrim()

        if (rank != null) {
            RankMedal(
                rank = rank,
                accent = accent,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
            )
        }
        if (creation.isBookmarked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Palette.Scrim),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = null,
                    tint = Palette.Accent,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.OnAccent,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Palette.GlassStroke, POSTER_SHAPE),
        )
    }
}

@Composable
private fun BoxScope.PosterScrim() {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(150.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        Palette.Canvas.copy(alpha = 0.6f),
                        Palette.Canvas.copy(alpha = 0.95f),
                    ),
                ),
            ),
    )
}

/** The `#1` badge worn by ranked cards — filled for the podium, outlined below it. */
@Composable
fun RankMedal(
    rank: Int,
    modifier: Modifier = Modifier,
    accent: Color = Palette.Accent,
) {
    val podium = rank <= 3
    Box(
        modifier = modifier
            .then(if (podium) Modifier.halo(accent, CircleShape, radius = 16.dp) else Modifier)
            .clip(CircleShape)
            .background(if (podium) accent else Palette.Scrim)
            .border(1.dp, if (podium) Color.Transparent else Palette.GlassStroke, CircleShape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text = "#$rank",
            color = if (podium) Palette.OnAccentDark else Palette.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
