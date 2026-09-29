package dev.mod.store.minecraft.core.ads

import dev.mod.store.minecraft.domain.config.AdPlacement

/**
 * The only surface sections see of the ad subsystem. Native list/fullscreen ads are the
 * `NativeSlot` composables; this covers fullscreen-on-navigation and the list ad cadence.
 */
interface ScreenAds {
    /** How many catalog items between inline native ads (0 = none). */
    val nativeInterval: Int

    /**
     * True when [placement] may run — native ads on for the app and this slot left on in the admin
     * panel — and at least one ad is buffered, so a slot can render immediately.
     */
    fun hasNativeAd(placement: AdPlacement): Boolean

    /**
     * Whether [placement] may run at all, regardless of whether an ad is loaded yet. False means
     * nothing will ever fill it, so waiting for one is waiting for something that cannot happen.
     */
    fun nativeAllowed(placement: AdPlacement): Boolean

    /** Called when the Spotlight screen is shown — may surface an interstitial. */
    fun onSpotlightEntered()

    /**
     * Suspends until the ad stack has finished booting — remote config fetched and the ad units
     * wired (or given up on). The splash gate waits on this before letting the user in.
     */
    suspend fun awaitBoot()
}

/** Identity + flavour values the controller needs, supplied by :app (which has BuildConfig). */
data class BillboardConfig(
    /** Kept for when the ad SDK returns; nothing reads it while the app ships without ads. */
    val casId: String,
    val metricaKey: String,
    val debug: Boolean,
)
