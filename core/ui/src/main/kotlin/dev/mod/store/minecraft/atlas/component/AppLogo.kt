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
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.LayerDrawable
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
            val icon = context.packageManager.getApplicationIcon(context.packageName)
            // An adaptive icon arrives already cut to the launcher's mask — a circle on most
            // phones. Painting its two layers ourselves gives the full square artwork back, and
            // the rounding is then ours to choose.
            if (icon is AdaptiveIconDrawable) {
                // Both layers, painted without the launcher's mask, then cropped to the safe zone
                // the artwork actually lives in. Keeping the whole square would show the outer band
                // of the background layer — the green frame every launcher hides under its mask.
                val full = LayerDrawable(arrayOf(icon.background, icon.foreground))
                    .toBitmap(width = SOURCE, height = SOURCE)
                val inset = (SOURCE - SOURCE * SAFE_ZONE) / 2f
                Bitmap.createBitmap(
                    full,
                    inset.toInt(),
                    inset.toInt(),
                    (SOURCE * SAFE_ZONE).toInt(),
                    (SOURCE * SAFE_ZONE).toInt(),
                ).asImageBitmap()
            } else {
                icon.toBitmap(width = SOURCE, height = SOURCE).asImageBitmap()
            }
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

/** Side of the bitmap the launcher icon is rendered into. */
private const val SOURCE = 288

/**
 * How much of an adaptive icon is guaranteed to hold artwork: 72 of its 108 units. The rest is
 * bleed the launcher mask eats, and showing it is what put a green band around the logo.
 */
private const val SAFE_ZONE = 72f / 108f
