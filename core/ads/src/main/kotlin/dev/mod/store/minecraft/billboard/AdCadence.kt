package dev.mod.store.minecraft.core.ads

/**
 * How many list items sit between two inline ads.
 *
 * The remote config carries a cadence, but it is only a hint here: whatever it says, the spacing is
 * clamped into a band. The floor keeps two ads from ever sharing a screen — an ad card runs to
 * roughly 380dp and a catalog card to 120dp, so [MIN] items between them puts more than a screen's
 * worth of catalog in the gap. The ceiling keeps a large remote value from thinning the lists out
 * to one ad at the very bottom, which is what they had been doing.
 *
 * The master switch is elsewhere: `adToggles.native` decides whether native ads run at all, and
 * when it is off no pool is started and no slot fills. A zero cadence therefore means "not
 * configured", not "never", and falls back to [DEFAULT].
 */
object AdCadence {

    /** Items between ads when the config has nothing useful to say. */
    const val DEFAULT = 4

    /** Closest two ads may ever be placed, whatever the config asks for. */
    const val MIN = 3

    /** Furthest apart they may be, so a list never runs dry of them. */
    const val MAX = 5

    fun of(configInterval: Int): Int =
        if (configInterval > 0) configInterval.coerceIn(MIN, MAX) else DEFAULT

    /** True when an ad belongs after the item at [index] (zero-based) at this [cadence]. */
    fun breaksAfter(index: Int, cadence: Int = DEFAULT): Boolean =
        cadence > 0 && (index + 1) % cadence == 0
}
