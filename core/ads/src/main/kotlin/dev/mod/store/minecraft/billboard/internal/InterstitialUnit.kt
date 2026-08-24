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
 * Interstitial wrapper. CAS autoloads/reloads itself (best fill rate); we only gate *showing*
 * with the server cooldown, the show-chance and the shared fullscreen gate.
 */
internal class InterstitialUnit(
    context: Context,
    private val casId: String,
    private val cooldownSeconds: Int,
    private val showChance: Int,
) {
    private companion object {
        const val TYPE = "Interstitial"
    }

    private val appContext = context.applicationContext
    private var interstitial: CASInterstitial? = null
    private var lastShownAt = 0L

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
