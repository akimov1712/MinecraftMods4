package dev.mod.store.minecraft.feature.showcase

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.component.MetaChip
import dev.mod.store.minecraft.core.ui.component.RemoteImage
import dev.mod.store.minecraft.core.ui.component.creationCategoryLabel
import dev.mod.store.minecraft.core.ui.effect.CardShape
import dev.mod.store.minecraft.core.ui.effect.SmallShape
import dev.mod.store.minecraft.core.ui.effect.popIn
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.core.ui.util.formatShortDate
import dev.mod.store.minecraft.domain.creation.CreationEntity
/**
 * The daily pick, presented as a wide banner: up to three frames of the mod's own artwork side
 * by side, the date stamped in the corner and the name reading across the bottom.
 */
@Composable
fun PickOfDayBanner(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val frames = remember(creation.id) {
        (listOf(creation.imageUrl) + creation.gallery)
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.72f)
            .clip(CardShape)
            .background(Palette.SurfaceHigh)
            .tappable(onClick = onClick),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            frames.forEachIndexed { index, url ->
                RemoteImage(
                    url = url,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentScale = ContentScale.Crop,
                )
                if (index < frames.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(Palette.Canvas),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Palette.Canvas.copy(alpha = 0.45f),
                        1f to Palette.Canvas.copy(alpha = 0.92f),
                    ),
                ),
        )

        Text(
            text = formatShortDate(System.currentTimeMillis()).uppercase(),
            color = Palette.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(SmallShape)
                .background(Palette.Canvas.copy(alpha = 0.75f))
                .padding(horizontal = 9.dp, vertical = 5.dp),
        )

        if (creation.isBookmarked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .popIn()
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Palette.Accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bookmark,
                    contentDescription = null,
                    tint = Palette.OnAccentDark,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = creationCategoryLabel(creation.category),
                    color = Palette.TextMuted,
                    fontSize = 14.sp,
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

/** Tile for the "just added" rail: artwork with a corner ribbon and the name below. */
@Composable
fun FreshCard(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(158.dp)
            .tappable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f)
                .clip(SmallShape)
                .background(Palette.SurfaceHigh),
        ) {
            RemoteImage(
                url = creation.imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = stringResource(R.string.showcase_fresh_badge),
                color = Palette.OnAccentDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .popIn()
                    .clip(SmallShape)
                    .background(Palette.Positive)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            )
        }
        Text(
            text = creation.title,
            color = Palette.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        creation.supportedVersions.firstOrNull()?.let { version ->
            Text(
                text = stringResource(dev.mod.store.minecraft.core.ui.R.string.creation_version_short, version),
                color = Palette.TextFaint,
                fontSize = 12.sp,
            )
        }
    }
}

/** Movement of a mod between the editorial order and the install chart. */
enum class ChartMove { Up, Down, Flat, New }

/**
 * The small green/red delta shown beside a chart position. A mod that has not moved says nothing —
 * a dash next to every other place is noise, not information.
 */
@Composable
fun ChartMoveTag(move: ChartMove, delta: Int, modifier: Modifier = Modifier) {
    if (move == ChartMove.Flat) return
    val (text, tint) = when (move) {
        ChartMove.Up -> "↑$delta" to Palette.Positive
        ChartMove.Down -> "↓$delta" to Palette.Negative
        ChartMove.New -> stringResource(R.string.showcase_chart_new) to Palette.Gold
        ChartMove.Flat -> return
    }
    Text(
        text = text,
        color = tint,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(SmallShape)
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}
