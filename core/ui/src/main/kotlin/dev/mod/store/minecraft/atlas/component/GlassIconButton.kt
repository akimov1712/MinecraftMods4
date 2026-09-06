package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.effect.CardBox
import dev.mod.store.minecraft.core.ui.theme.Palette

/** A round icon button: back, close, bookmark. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 40.dp,
    tint: Color = Palette.TextPrimary,
    container: Color = Palette.SurfaceHigh,
) {
    CardBox(
        modifier = modifier.size(size),
        fill = container,
        shape = CircleShape,
        onClick = onClick,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .align(Alignment.Center)
                .size(size * 0.46f),
        )
    }
}
