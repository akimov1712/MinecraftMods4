package dev.mod.store.minecraft.core.ads

/**
 * The only surface sections see of the ad subsystem. Native list/fullscreen ads are the
 * `NativeSlot` composables; this covers fullscreen-on-navigation and the list ad cadence.
 */
interface ScreenAds {
    /** How many catalog items between inline native ads (0 = none). */
    val nativeInterval: Int

    /** True once at least one native ad is buffered and a slot can render immediately. */
    val hasNativeAd: Boolean

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
    val casId: String,
    val metricaKey: String,
    val debug: Boolean,
)
