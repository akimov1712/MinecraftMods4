package dev.mod.store.minecraft.core.ads.internal

import android.app.Activity
import android.content.Context
import com.cleveradssolutions.sdk.AdContentInfo
import com.cleveradssolutions.sdk.AdFormat
import com.cleveradssolutions.sdk.OnAdImpressionListener
import com.cleveradssolutions.sdk.screen.CASInterstitial
import com.cleveradssolutions.sdk.screen.ScreenAdContentCallback
import com.cleversolutions.ads.AdError

/**
 * Interstitial wrapper. CAS autoloads/reloads itself (best fill rate); we only gate *showing* with
 * the server cooldown, the show-chance, the shared fullscreen gate, and the opening grace period.
 */
internal class InterstitialUnit(
    context: Context,
    private val casId: String,
    private val cooldownSeconds: Int,
    private val showChance: Int,
    private val skipsBeforeFirst: Int,
) {
    private companion object {
        const val TYPE = "Interstitial"
    }

    private val appContext = context.applicationContext
    private var interstitial: CASInterstitial? = null
    private var lastShownAt = 0L

    /** Requests swallowed so far by the opening grace period; never persisted. */
    private var skipped = 0

    fun load() {
        interstitial = CASInterstitial(casId).apply {
            contentCallback = callback
            isAutoloadEnabled = true
            isAutoshowEnabled = false
            minInterval = 0
            onImpressionListener = OnAdImpressionListener { ad -> AdLog.impression(TYPE, ad) }
        }
        interstitial?.load(appContext)
    }

    fun tryShow(activity: Activity) {
        // Counted before the chance roll, so a skipped opening never burns a roll: with a grace of
        // 2, the third request is the first that may show, whatever the chance says.
        if (skipped < skipsBeforeFirst) {
            skipped++
            return
        }
        if (!rollChance(showChance)) return
        if (System.currentTimeMillis() - lastShownAt < cooldownSeconds * 1000L) return
        if (!FullscreenGate.canShow()) return
        val ad = interstitial ?: return
        if (ad.isLoaded) {
            FullscreenGate.markShown()
            ad.show(activity)
        }
    }

    fun destroy() {
        interstitial?.destroy()
        interstitial = null
    }

    private val callback = object : ScreenAdContentCallback() {
        override fun onAdLoaded(ad: AdContentInfo) = AdLog.loaded(TYPE, ad)
        override fun onAdShowed(ad: AdContentInfo) = Unit
        override fun onAdDismissed(ad: AdContentInfo) {
            AdLog.dismissed(TYPE)
            lastShownAt = System.currentTimeMillis()
        }
        override fun onAdFailedToLoad(format: AdFormat, error: AdError) = AdLog.failed("$TYPE load", error)
        override fun onAdFailedToShow(format: AdFormat, error: AdError) = AdLog.failed("$TYPE show", error)
        override fun onAdClicked(ad: AdContentInfo) = AdLog.clicked(TYPE, ad)
    }
}
