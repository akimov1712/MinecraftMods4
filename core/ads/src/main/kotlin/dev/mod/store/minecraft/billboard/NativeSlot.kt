package dev.mod.store.minecraft.core.ads

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnAttach
import com.cleveradssolutions.sdk.nativead.NativeAdContent
import dev.mod.store.minecraft.core.ads.internal.NativeAdViews
import dev.mod.store.minecraft.core.ads.internal.NativePool
import dev.mod.store.minecraft.core.ads.internal.buildNativeAdViews
import dev.mod.store.minecraft.core.ads.internal.rollChance
import dev.mod.store.minecraft.domain.config.AdPlacement
import kotlinx.coroutines.delay

private const val ACQUIRE_ATTEMPTS = 10
private const val ACQUIRE_RETRY_MS = 500L

internal class NativeHolder(val views: NativeAdViews, val ad: NativeAdContent)

/**
 * Process-wide registry of native ad slots, populated by the billboard controller once config is
 * loaded. The Compose slots below read from it. Banner mode is not used — native only.
 */
internal object NativeRegistry {

    private var pool: NativePool? = null
    private var chance: Int = 100
    private val cache = HashMap<String, NativeHolder>()
    private var placements: Set<AdPlacement> = AdPlacement.entries.toSet()
    private val fullscreenRolls = HashMap<AdPlacement, Boolean>()

    var enabled: Boolean = false
        private set

    /** How many list items to place between two inline native slots (0 = never). */
    var interval: Int = 0
        private set

    fun configure(pool: NativePool, chance: Int, interval: Int, placements: Set<AdPlacement>) {
        this.pool = pool
        this.chance = chance
        this.interval = interval
        this.placements = placements
        enabled = true
    }

    /**
     * Whether this slot may run at all: native ads switched on for the app, and this particular
     * slot left on in the admin panel. Independent of whether an ad happens to be loaded.
     */
    fun allows(placement: AdPlacement): Boolean = enabled && placement in placements

    fun rollShow(placement: AdPlacement): Boolean = allows(placement) && rollChance(chance)

    /**
     * The show-chance for a slot that takes the whole screen, rolled once per launch and then
     * remembered.
     *
     * An inline slot rolls per slot key, which is what thins ads out across a list. A full-screen
     * one is a single event, and the same answer has to serve two callers: the decision to route to
     * the screen ([hasAd]) and the screen's own slot. Rolling twice could route a reader to a promo
     * page that then declines to draw anything.
     */
    private fun rollFullscreen(placement: AdPlacement): Boolean =
        fullscreenRolls.getOrPut(placement) { rollChance(chance) }

    /** Whether [placement] may run, chance included for the full-screen slots. */
    fun permits(placement: AdPlacement): Boolean =
        allows(placement) && (!placement.fullscreen || rollFullscreen(placement))

    /** Whether a preloaded ad is sitting in the pool right now. */
    fun hasAd(): Boolean = pool?.hasAd() == true

    /** Whether [placement] may run *and* has something to show this instant. */
    fun hasAd(placement: AdPlacement): Boolean = permits(placement) && hasAd()

    fun acquire(context: Context, key: String, fullscreen: Boolean): NativeHolder? {
        cache[key]?.let { return it }
        val ad = pool?.pop() ?: return null
        val holder = NativeHolder(buildNativeAdViews(context, fullscreen), ad)
        cache[key] = holder
        return holder
    }

    fun reset() {
        cache.values.forEach { runCatching { it.ad.destroy() } }
        cache.clear()
        pool = null
        enabled = false
        interval = 0
        placements = AdPlacement.entries.toSet()
        fullscreenRolls.clear()
    }
}

/**
 * Inline native ad for use between list items. Renders nothing when the slot is switched off in the
 * admin panel, when the chance roll goes against it, or when no ad is available.
 *
 * Call sites that draw their own wrapper around the slot — padding, a divider, a spacer — should
 * ask [dev.mod.store.minecraft.core.ads.ScreenAds.hasNativeAd] about the same placement first, or
 * the wrapper is left behind around nothing.
 */
@Composable
fun NativeSlot(placement: AdPlacement, slotKey: String, modifier: Modifier = Modifier) {
    val show = remember(slotKey) { NativeRegistry.rollShow(placement) }
    if (!show) return
    AdHost(slotKey = slotKey, fullscreen = false, modifier = modifier)
}

/**
 * The filling form of a native ad: artwork stretches to whatever height it is given and the panel
 * of copy keeps its own size. Use it anywhere the slot has a bounded height — the whole screen, or
 * one box in a column — because unlike [NativeSlot] it can never be taller than its container and
 * so can never have its bottom clipped off.
 */
@Composable
fun FullscreenNativeSlot(
    placement: AdPlacement,
    slotKey: String,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    if (!NativeRegistry.permits(placement)) return
    AdHost(slotKey = slotKey, fullscreen = true, modifier = modifier)
}

@Composable
private fun AdHost(slotKey: String, fullscreen: Boolean, modifier: Modifier) {
    val context = LocalContext.current
    var holder by remember(slotKey) {
        mutableStateOf(NativeRegistry.acquire(context, slotKey, fullscreen))
    }

    LaunchedEffect(slotKey) {
        var attempt = 0
        while (holder == null && attempt < ACQUIRE_ATTEMPTS) {
            delay(ACQUIRE_RETRY_MS)
            holder = NativeRegistry.acquire(context, slotKey, fullscreen)
            attempt++
        }
    }

    val resolved = holder ?: return
    AndroidView(
        modifier = modifier,
        factory = {
            (resolved.views.root.parent as? ViewGroup)?.removeView(resolved.views.root)
            resolved.views.root.doOnAttach {
                if (!resolved.views.bound) {
                    resolved.views.bound = true
                    resolved.views.bind(resolved.ad)
                }
            }
            resolved.views.root
        },
    )
}
