package dev.mod.store.minecraft.core.ads

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.mod.store.minecraft.domain.config.AdPlacement

/*
 * The app ships without advertising, so the ad SDK is not a dependency any more and these two are
 * the seams that are left: the shape every screen calls, drawing nothing.
 *
 * They are kept so that bringing ads back is one commit rather than a hunt through ten screens —
 * the placements, the remote switches and the cadence rules all still exist. Nothing reaches them
 * today: every call site asks [ScreenAds] first, and it answers that there is nothing to show.
 */

/** Inline native ad between list items. Draws nothing while the app carries no ad SDK. */
@Composable
fun NativeSlot(placement: AdPlacement, slotKey: String, modifier: Modifier = Modifier) = Unit

/** The full-screen form of a native ad. Draws nothing while the app carries no ad SDK. */
@Composable
fun FullscreenNativeSlot(
    placement: AdPlacement,
    slotKey: String,
    modifier: Modifier = Modifier.fillMaxSize(),
) = Unit
