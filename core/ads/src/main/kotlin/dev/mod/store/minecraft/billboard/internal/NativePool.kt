package dev.mod.store.minecraft.core.ads.internal

import android.content.Context
import com.cleveradssolutions.sdk.AdContentInfo
import com.cleveradssolutions.sdk.nativead.AdChoicesPlacement
import com.cleveradssolutions.sdk.nativead.CASNativeLoader
import com.cleveradssolutions.sdk.nativead.NativeAdContent
import com.cleveradssolutions.sdk.nativead.NativeAdContentCallback
import com.cleversolutions.ads.AdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.pow

/**
 * Keeps a small buffer of preloaded native ads ready for list/fullscreen slots, refilling as
 * they're consumed, with timeout recovery and exponential backoff on failures.
 */
internal class NativePool(
    context: Context,
    private val casId: String,
    private val poolSize: Int,
) {
    private companion object {
        const val TYPE = "Native"
        const val REFILL_THRESHOLD = 2
        const val LOAD_TIMEOUT_MS = 12_000L
        const val MAX_PARALLEL_LOADS = 2
        const val MAX_RETRY_BEFORE_RECREATE = 5
        const val MAX_BACKOFF_EXP = 6
    }

    private val appContext = context.applicationContext
    private val loaded = ArrayDeque<NativeAdContent>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var loader: CASNativeLoader? = null
    private var retryJob: Job? = null
    private var destroyed = false
    private var retryAttempt = 0
    private var loadingCount = 0
    private var loadStartedAt = 0L

    fun start() {
        createLoader()
        loadNext()
    }

    fun hasAd(): Boolean = loaded.isNotEmpty()

    fun pop(): NativeAdContent? {
        val ad = loaded.removeFirstOrNull()
        maybeRefill()
        return ad
    }

    fun destroy() {
        destroyed = true
        retryJob?.cancel()
        scope.cancel()
        loaded.forEach { it.destroy() }
        loaded.clear()
        loader = null
    }

    private fun maybeRefill() {
        if (destroyed || loaded.size > REFILL_THRESHOLD) return
        loadNext()
    }

    private fun loadNext() {
        if (destroyed || loaded.size >= poolSize || loadingCount >= MAX_PARALLEL_LOADS) return

        val now = System.currentTimeMillis()
        if (loader?.isLoading == true) {
            if (now - loadStartedAt > LOAD_TIMEOUT_MS) recreateLoader() else return
        }

        loadingCount++
        loadStartedAt = now
        loader?.load(1)
    }

    private fun recreateLoader() {
        loader = null
        createLoader()
        retryAttempt = 0
        loadingCount = 0
        loadStartedAt = 0L
    }

    private fun createLoader() {
        loader = CASNativeLoader(appContext, casId, callback).apply {
            adChoicesPlacement = AdChoicesPlacement.TOP_LEFT
            isStartVideoMuted = true
        }
    }

    private val callback = object : NativeAdContentCallback() {
        override fun onNativeAdLoaded(nativeAd: NativeAdContent, ad: AdContentInfo) {
            AdLog.loaded(TYPE, ad)
            loadingCount = maxOf(0, loadingCount - 1)
            loadStartedAt = 0L
            retryAttempt = 0
            if (destroyed) {
                nativeAd.destroy()
                return
            }
            if (loaded.size < poolSize) loaded.add(nativeAd) else nativeAd.destroy()
            loadNext()
        }

        override fun onNativeAdFailedToLoad(error: AdError) {
            AdLog.failed("$TYPE load", error)
            loadingCount = maxOf(0, loadingCount - 1)
            loadStartedAt = 0L
            retryAttempt++
            if (retryAttempt >= MAX_RETRY_BEFORE_RECREATE) recreateLoader()
            val delayMs = 2.0.pow(min(retryAttempt, MAX_BACKOFF_EXP)).toLong() * 1000
            retryJob?.cancel()
            retryJob = scope.launch {
                delay(delayMs)
                loadNext()
            }
        }

        override fun onNativeAdFailedToShow(nativeAd: NativeAdContent, error: AdError) {
            AdLog.failed("$TYPE show", error)
            nativeAd.destroy()
        }

        override fun onNativeAdClicked(nativeAd: NativeAdContent, ad: AdContentInfo) =
            AdLog.clicked(TYPE, ad)
    }
}
