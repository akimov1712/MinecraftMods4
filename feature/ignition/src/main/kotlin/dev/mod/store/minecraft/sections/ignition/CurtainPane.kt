package dev.mod.store.minecraft.feature.ignition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ads.FullscreenNativeSlot
import dev.mod.store.minecraft.core.ui.R
import dev.mod.store.minecraft.core.ui.effect.tappable
import dev.mod.store.minecraft.core.ui.theme.Palette

/**
 * The one full-screen promo between the splash and the catalog. Reached only when an ad is already
 * loaded; if the pool has emptied in the meantime the screen lets itself out rather than showing a
 * blank page, and the close key is on screen from the first frame either way.
 *
 * Everything here is square — the key included — because the ad it frames is square, and because
 * this is still the splash's half of the app rather than the catalog's.
 */
@Composable
fun CurtainPane(
    component: CurtainComponent,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        if (!component.hasNativeAd) component.close()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.Canvas),
    ) {
        // The ad view is a plain Android view with no idea about system bars, so it is inset here.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            FullscreenNativeSlot(slotKey = "curtain")
        }

        CloseKey(onClick = component::close)
    }
}

/**
 * Square, bordered, and painted after the press modifier so the plate never separates from the
 * glyph when it is tapped.
 */
@Composable
private fun BoxScope.CloseKey(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(12.dp)
            .size(40.dp)
            .tappable(pressedScale = 0.9f, onClick = onClick)
            .drawBehind {
                drawRect(Palette.Surface)
                drawRect(
                    color = Palette.Stroke,
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.ignition_curtain_close),
            tint = Palette.TextPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}
