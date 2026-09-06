package dev.mod.store.minecraft.core.ui.effect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.theme.Palette

/** The default rounding used by cards. */
val CardShape = RoundedCornerShape(16.dp)

/** Tighter rounding for small things: chips, thumbnails, buttons. */
val SmallShape = RoundedCornerShape(11.dp)

/** A plain filled surface with soft corners — the only container the app uses. */
fun Modifier.card(
    fill: Color = Palette.Surface,
    shape: Shape = CardShape,
): Modifier = this
    .clip(shape)
    .background(fill)

/**
 * A tappable card. Everything a child can press is one of these: a big target, a soft shape and
 * a gentle press animation, nothing else.
 */
@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    fill: Color = Palette.Surface,
    shape: Shape = CardShape,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .then(
                if (onClick != null) {
                    Modifier.tappable(enabled = enabled, pressedScale = 0.97f, onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content,
    )
}

/** A soft coloured glow under a surface — used sparingly, on the things worth pressing. */
fun Modifier.halo(
    color: Color,
    shape: Shape,
    radius: Dp = 18.dp,
    alpha: Float = 0.5f,
): Modifier = shadow(
    elevation = radius,
    shape = shape,
    clip = false,
    ambientColor = color.copy(alpha = alpha),
    spotColor = color.copy(alpha = alpha),
)
