package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dev.mod.store.minecraft.core.ui.theme.Palette

/**
 * The app's own launcher icon, drawn as the logotype. Every flavour ships its own icon, so the
 * splash always shows the right brand without a second asset to keep in sync.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    /** Defaults to the app's usual rounding; the splash asks for a square. */
    shape: Shape = RoundedCornerShape(size / 4),
) {
    val context = LocalContext.current
    val icon: ImageBitmap? = remember(context) {
        runCatching {
            context.packageManager
                .getApplicationIcon(context.packageName)
                .toBitmap(width = 288, height = 288)
                .asImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Palette.SurfaceHigh),
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        }
    }
}
