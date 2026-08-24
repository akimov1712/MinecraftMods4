package dev.mod.store.minecraft.core.ads.internal

/** Rolls a 0..99 die against a percent chance (0 never, 100 always). */
internal fun rollChance(percent: Int): Boolean = (0 until 100).random() < percent

/**
 * Process-wide guard so two fullscreen ads (interstitial / app-open) can't stack back-to-back —
 * a pattern that gets ad accounts suspended. A single shared timestamp plus a minimum gap.
 */
internal object FullscreenGate {

    private const val MIN_GAP_MS = 15_000L

    @Volatile
    private var lastShownAt = 0L

    fun canShow(): Boolean = System.currentTimeMillis() - lastShownAt >= MIN_GAP_MS

    fun markShown() {
        lastShownAt = System.currentTimeMillis()
    }
}
