package dev.mod.store.minecraft.domain.config

/** Server-driven monetization configuration consumed by :billboard. */
data class ConfigEntity(
    val adToggles: AdToggles,
    val adChance: AdChance,
    val interstitialCooldownSeconds: Int,
    val nativePreloadSize: Int,
    val nativeInterval: Int,
    val nativeKind: NativeKind,
    /**
     * Native slots the panel left switched on. Defaults to all of them, which is what an older
     * server with no `ads` array — or a first launch with no cached list — means.
     */
    val enabledPlacements: Set<AdPlacement> = AdPlacement.entries.toSet(),
) {

    /** A slot is drawn only when the master native switch and its own switch are both on. */
    fun isPlacementEnabled(placement: AdPlacement): Boolean =
        adToggles.native && placement in enabledPlacements
}

/** Master on/off switches per ad placement. */
data class AdToggles(
    val appOpen: Boolean,
    val native: Boolean,
    val interstitial: Boolean,
)

/** Probability (0..100) that a given placement is actually shown when eligible. */
data class AdChance(
    val appOpen: Int,
    val native: Int,
    val interstitial: Int,
)

/** Visual form requested for native slots. */
enum class NativeKind {
    BANNER,
    NATIVE;

    companion object {
        fun fromRaw(value: String?): NativeKind =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NATIVE
    }
}
