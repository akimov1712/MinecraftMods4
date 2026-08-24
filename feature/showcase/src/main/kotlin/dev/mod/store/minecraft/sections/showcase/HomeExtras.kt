package dev.mod.store.minecraft.feature.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.FiberNew
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.theme.Palette
import java.util.Calendar

/** How a chart entry moved relative to the editorial (trending) order. */
enum class ChartMove { Up, Down, Flat, New }

/** Arrow shown at the end of a chart row — green up, rose down, dash for "unchanged". */
@Composable
fun ChartMoveBadge(move: ChartMove, modifier: Modifier = Modifier) {
    val (icon, tint) = when (move) {
        ChartMove.Up -> Icons.Rounded.ArrowUpward to Palette.Positive
        ChartMove.Down -> Icons.Rounded.ArrowDownward to Palette.Negative
        ChartMove.Flat -> Icons.Rounded.Remove to Palette.TextMuted
        ChartMove.New -> Icons.Rounded.FiberNew to Palette.Gold
    }
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(15.dp),
        )
    }
}

/** Chips listing the Minecraft versions the catalog currently covers. */
@Composable
fun VersionsStrip(versions: List<String>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Palette.Surface)
            .border(1.dp, Palette.GlassStroke, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.showcase_versions_title),
            color = Palette.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            versions.forEach { version ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Palette.SurfaceHigh)
                        .border(1.dp, Palette.Stroke, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = version,
                        color = Palette.TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/** A rotating hint — the same one for the whole day, a new one tomorrow. */
@Composable
fun TipCard(modifier: Modifier = Modifier) {
    val tips = stringArrayResource(R.array.showcase_tips)
    val index = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Palette.Gold.copy(alpha = 0.10f))
            .border(1.dp, Palette.Gold.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Palette.Gold.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                tint = Palette.Gold,
                modifier = Modifier.size(19.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.showcase_tip_title),
                color = Palette.Gold,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = tips[index % tips.size],
                color = Palette.TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }
    }
}

/** Closing line of the home feed. */
@Composable
fun FeedFooter(total: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.showcase_footer_title),
            color = Palette.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.showcase_footer_body, total),
            color = Palette.TextFaint,
            fontSize = 11.sp,
        )
    }
}
