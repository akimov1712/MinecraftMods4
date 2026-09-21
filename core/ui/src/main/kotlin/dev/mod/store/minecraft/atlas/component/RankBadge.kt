package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mod.store.minecraft.core.ui.theme.Palette

/** The metal a place is worth: gold, silver, bronze, then nothing. */
private fun podium(rank: Int): Color? = when (rank) {
    1 -> Palette.Podium1
    2 -> Palette.Podium2
    3 -> Palette.Podium3
    else -> null
}

/**
 * A pennant: soft shoulders at the top and a V cut out of the foot, the shape a rank ribbon has
 * always had. It is taller than it is wide, which a disc never is, so a place reads as a place
 * rather than as one more round chip in a list full of them.
 */
private val PennantShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val shoulder = with(density) { 7.dp.toPx() }
        val notch = size.height * 0.24f
        val path = Path().apply {
            moveTo(shoulder, 0f)
            lineTo(size.width - shoulder, 0f)
            quadraticTo(size.width, 0f, size.width, shoulder)
            lineTo(size.width, size.height)
            lineTo(size.width / 2f, size.height - notch)
            lineTo(0f, size.height)
            lineTo(0f, shoulder)
            quadraticTo(0f, 0f, shoulder, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A place in a chart.
 *
 * The top three are struck in metal, lit from above so the fill has some depth to it; every place
 * below them wears the same pennant in plain surface with a hairline around it. The eye sorts the
 * podium out at a glance, and the rest of the list states its number without shouting.
 */
@Composable
fun RankBadge(
    rank: Int,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
) {
    val metal = podium(rank)
    val width = size
    val height = size * 1.28f

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(PennantShape)
            .background(
                if (metal != null) {
                    // Lit from the top edge, the way a struck medal catches light.
                    Brush.verticalGradient(
                        listOf(metal.copy(alpha = 1f), metal.copy(alpha = 0.78f)),
                    )
                } else {
                    Brush.verticalGradient(listOf(Palette.SurfaceHigh, Palette.Surface))
                },
            )
            .then(
                if (metal == null) Modifier.border(1.dp, Palette.Stroke, PennantShape) else Modifier,
            )
            // The number sits in the body of the pennant, clear of the notch at its foot.
            .padding(bottom = height * 0.2f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank.toString(),
            color = if (metal != null) Palette.Canvas else Palette.TextMuted,
            fontSize = if (rank > 99) 12.sp else 15.sp,
            fontWeight = if (metal != null) FontWeight.Black else FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
