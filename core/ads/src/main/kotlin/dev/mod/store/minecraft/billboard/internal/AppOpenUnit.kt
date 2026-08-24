package dev.mod.store.minecraft.core.ads.internal

import android.app.Activity
import android.content.Context
import com.cleveradssolutions.sdk.AdContentInfo
import com.cleveradssolutions.sdk.AdFormat
import com.cleveradssolutions.sdk.OnAdImpressionListener
import com.cleveradssolutions.sdk.screen.CASAppOpen
import com.cleveradssolutions.sdk.screen.ScreenAdContentCallback
import com.cleversolutions.ads.AdError

/** App-open wrapper, shown when the app returns to the foreground. */
internal class AppOpenUnit(
    context: Context,
    private val casId: String,
    private val cooldownSeconds: Int,
    private val showChance: Int,
) {
    private companion object {
        const val TYPE = "AppOpen"
    }

    private val appContext = context.applicationContext
    private var appOpen: CASAppOpen? = null
    private var lastShownAt = 0L

    fun load() {
        appOpen = CASAppOpen(casId).apply {
            contentCallback = callback
            isAutoloadEnabled = true
            isAutoshowEnabled = false
            onImpressionListener = OnAdImpressionListener { ad -> AdLog.impression(TYPE, ad) }
        }
        appOpen?.load(appContext)
    }

    fun show(activity: Activity) {
        if (!rollChance(showChance)) return
        if (System.currentTimeMillis() - lastShownAt < cooldownSeconds * 1000L) return
        if (!FullscreenGate.canShow()) return
        val ad = appOpen ?: return
        if (ad.isLoaded) {
            FullscreenGate.markShown()
            ad.show(activity)
        }
    }

    fun destroy() {
        appOpen?.destroy()
        appOpen = null
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
