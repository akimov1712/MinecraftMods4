package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import dev.mod.store.minecraft.core.ui.theme.Palette

/**
 * Network image loader (Landscapist + Coil3). Shows the shimmer sweep while loading and a flat
 * surface on failure, so callers never deal with placeholders themselves.
 */
@Composable
fun RemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = RectangleShape,
) {
    CoilImage(
        imageModel = { url },
        modifier = modifier.clip(shape),
        imageOptions = ImageOptions(
            contentScale = contentScale,
            alignment = Alignment.Center,
            contentDescription = contentDescription,
        ),
        loading = { ShimmerBox(modifier = Modifier.matchParentSize(), shape = shape) },
        failure = { Box(Modifier.matchParentSize().background(Palette.Surface)) },
    )
}
