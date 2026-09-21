package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.effect.CardShape
import dev.mod.store.minecraft.core.ui.effect.SmallShape

/**
 * Placeholders shaped like the things they stand in for.
 *
 * A skeleton that does not match its content is worse than none: the page jumps when the real card
 * lands, and the wait reads as a glitch rather than as loading. Each of these mirrors one real
 * component — same sizes, same shapes, same gaps — so the swap is invisible.
 */

/** Stands in for [CreationCard]: wide cover left, three lines of varying length right. */
@Composable
fun CreationCardSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ShimmerBox(
            modifier = Modifier
                .width(150.dp)
                .aspectRatio(1.35f),
            shape = SmallShape,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                shape = SmallShape,
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(18.dp),
                shape = SmallShape,
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(22.dp),
                shape = SmallShape,
            )
        }
    }
}

/**
 * Stands in for [CreationPoster]: portrait artwork with a name and one line of numbers below. The
 * caller sets the width — a rail gives it a fixed one, a grid row gives it a weight.
 */
@Composable
fun CreationPosterSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.82f),
            shape = SmallShape,
        )
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(15.dp),
            shape = SmallShape,
        )
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(13.dp),
            shape = SmallShape,
        )
    }
}

/** Stands in for [CreationRow]: the chart line — place, thumbnail, name, numbers. */
@Composable
fun CreationRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ShimmerBox(modifier = Modifier.size(30.dp), shape = CircleShape)
        ShimmerBox(modifier = Modifier.size(64.dp), shape = SmallShape)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(17.dp),
                shape = SmallShape,
            )
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(13.dp),
                shape = SmallShape,
            )
        }
    }
}

/** Stands in for a titled section: the header glyph, its name, and the rail underneath. */
@Composable
fun RailSkeleton(
    modifier: Modifier = Modifier,
    tiles: Int = 3,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShimmerBox(modifier = Modifier.size(34.dp), shape = RoundedCornerShape(7.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(150.dp)
                    .height(24.dp),
                shape = SmallShape,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(tiles) {
                CreationPosterSkeleton(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Stands in for the daily banner. */
@Composable
fun BannerSkeleton(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.72f),
        shape = CardShape,
    )
}
