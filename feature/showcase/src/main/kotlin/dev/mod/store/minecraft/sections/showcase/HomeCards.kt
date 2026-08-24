package dev.mod.store.minecraft.feature.showcase

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.component.CategoryChip
import dev.mod.store.minecraft.core.ui.component.MetaChip
import dev.mod.store.minecraft.core.ui.component.RemoteImage
import dev.mod.store.minecraft.core.ui.component.creationCategoryAccent
import dev.mod.store.minecraft.core.ui.component.creationCategoryIcon
import dev.mod.store.minecraft.core.ui.component.creationCategoryLabel
import dev.mod.store.minecraft.core.ui.component.creationCategoryShade
import dev.mod.store.minecraft.core.ui.effect.halo
import dev.mod.store.minecraft.core.ui.effect.shine
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.formatCompact
import dev.mod.store.minecraft.core.ui.util.formatCount
import dev.mod.store.minecraft.core.ui.util.formatRating
import dev.mod.store.minecraft.core.ui.util.formatRelativeTime
import dev.mod.store.minecraft.core.ui.util.formatShortDate
import dev.mod.store.minecraft.domain.creation.CreationCategory
import dev.mod.store.minecraft.domain.creation.CreationEntity

private val HERO_SHAPE = RoundedCornerShape(30.dp)

/**
 * The pick of the day: a full-bleed poster with a breathing accent border, the badge that dates
 * it, and everything worth knowing about the creation stacked over the artwork.
 */
@Composable
fun PickOfDayCard(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "pick-glow")
    val glow by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Reverse),
        label = "pick-glow-alpha",
    )
    val accent = creationCategoryAccent(creation.category)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.94f)
            .clip(HERO_SHAPE)
            .background(Palette.Surface)
            .tappable(onClick = onClick),
    ) {
        RemoteImage(
            url = creation.imageUrl,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Palette.Canvas.copy(alpha = 0.6f),
                        0.32f to Color.Transparent,
                        0.58f to Palette.Canvas.copy(alpha = 0.65f),
                        1f to Palette.Canvas.copy(alpha = 0.97f),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PickBadge()
            MetaChip(
                text = formatShortDate(System.currentTimeMillis()),
                tint = Palette.TextPrimary,
                container = Palette.Scrim,
            )
            Box(Modifier.weight(1f))
            if (creation.isBookmarked) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Palette.Scrim),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bookmark,
                        contentDescription = null,
                        tint = Palette.Accent,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CategoryChip(creation.category)
            Text(
                text = creation.title,
                color = Palette.OnAccent,
                fontSize = 23.sp,
                lineHeight = 28.sp,
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
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Palette.Accent, Palette.AccentDeep)))
                    .tappable(onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.showcase_pick_action),
                    color = Palette.OnAccentDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Palette.OnAccentDark,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = glow),
                            Palette.Accent.copy(alpha = glow * 0.7f),
                            Color.Transparent,
                        ),
                    ),
                    shape = HERO_SHAPE,
                ),
        )
    }
}

@Composable
private fun PickBadge() {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Palette.EmberGradient)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = stringResource(R.string.showcase_pick_badge),
            color = Color.Black.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** One counter in the stats strip under the hero. */
@Composable
fun StatTile(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.Surface)
            .border(1.dp, Palette.GlassStroke, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = value,
            color = Palette.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
        Text(
            text = label,
            color = Palette.TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            lineHeight = 13.sp,
        )
    }
}

/** Landscape card used by the "fresh drops" rail, with its own age stamp. */
@Composable
fun FreshCard(
    creation: CreationEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(238.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Palette.Surface)
            .border(1.dp, Palette.GlassStroke, RoundedCornerShape(24.dp))
            .tappable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp),
        ) {
            RemoteImage(url = creation.imageUrl, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(Palette.Accent)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = stringResource(R.string.showcase_fresh_badge),
                    color = Palette.OnAccentDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = creation.title,
                color = Palette.TextPrimary,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CategoryChip(creation.category)
                creation.publishedAtEpochMs?.let { published ->
                    MetaChip(text = formatRelativeTime(published), tint = Palette.TextMuted)
                }
            }
        }
    }
}
