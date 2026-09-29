package dev.mod.store.minecraft.core.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dev.mod.store.minecraft.core.ads.internal.Analytics
import dev.mod.store.minecraft.core.ads.internal.AppOpenUnit
import dev.mod.store.minecraft.core.ads.internal.InterstitialUnit
import dev.mod.store.minecraft.core.ads.internal.NativePool
import dev.mod.store.minecraft.core.ads.internal.ReviewPrompter
import dev.mod.store.minecraft.core.ads.internal.initializeCas
import dev.mod.store.minecraft.domain.config.AdPlacement
import dev.mod.store.minecraft.domain.config.ConfigEntity
import dev.mod.store.minecraft.domain.config.FetchConfigUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Boots and coordinates the whole ad subsystem. Started once from the Application. Tracks the
 * current Activity itself so [ScreenAds] callers (Decompose components) never need to hold one.
 */
class BillboardController internal constructor(
    private val application: Application,
    private val fetchConfig: FetchConfigUseCase,
    private val reviewPrompter: ReviewPrompter,
    private val analytics: Analytics,
    private val config: BillboardConfig,
) : ScreenAds {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val booted = CompletableDeferred<Unit>()

    private var interstitial: InterstitialUnit? = null
    private var appOpen: AppOpenUnit? = null

    private var currentActivity: Activity? = null
    private var wasBackgrounded = false

    /**
     * Set when the process comes back to the foreground, spent on the next resumed Activity.
     *
     * The return itself cannot show the ad: ProcessLifecycleOwner reports ON_START from the
     * Activity's onStart, one callback *before* onResume, and [currentActivity] is cleared on
     * pause — so at that moment there is no Activity to show anything on, and the app-open ad was
     * being dropped every single time. Waiting for the resume also satisfies the SDK, which needs a
     * resumed Activity to put a full-screen ad on.
     */
    private var appOpenPending = false
    private var reviewRequested = false
    private var started = false

    override val nativeInterval: Int get() = NativeRegistry.interval

    override fun hasNativeAd(placement: AdPlacement): Boolean = NativeRegistry.hasAd(placement)

    override fun nativeAllowed(placement: AdPlacement): Boolean = NativeRegistry.permits(placement)

    override suspend fun awaitBoot() = booted.await()

    override fun onSpotlightEntered() {
        currentActivity?.let { interstitial?.tryShow(it) }
    }

    fun start() {
        if (started) return
        started = true

        analytics.start(application, config.metricaKey)
        application.registerActivityLifecycleCallbacks(activityTracker)
        ProcessLifecycleOwner.get().lifecycle.addObserver(foregroundObserver)

        scope.launch {
            try {
                val settings = fetchConfig()
                withContext(Dispatchers.IO) {
                    application.initializeCas(config.casId, settings, config.debug)
                }
                wireAds(settings)
            } finally {
                booted.complete(Unit)
            }
        }
    }

    private fun wireAds(settings: ConfigEntity) {
        if (settings.adToggles.interstitial) {
            interstitial = InterstitialUnit(
                context = application,
                casId = config.casId,
                cooldownSeconds = settings.interstitialCooldownSeconds,
                showChance = settings.adChance.interstitial,
                skipsBeforeFirst = settings.interstitialSkipsBeforeFirst,
            ).also { it.load() }
        }
        if (settings.adToggles.appOpen) {
            appOpen = AppOpenUnit(
                context = application,
                casId = config.casId,
                cooldownSeconds = settings.interstitialCooldownSeconds,
                showChance = settings.adChance.appOpen,
            ).also { it.load() }
        }
        if (settings.adToggles.native) {
            val pool = NativePool(application, config.casId, settings.nativePreloadSize).also { it.start() }
            NativeRegistry.configure(
                pool = pool,
                chance = settings.adChance.native,
                interval = settings.nativeInterval,
                placements = settings.enabledPlacements,
            )
        }
    }

    private val foregroundObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> wasBackgrounded = true
            Lifecycle.Event.ON_START -> {
                if (wasBackgrounded) {
                    wasBackgrounded = false
                    // A cold start never gets here — the splash owns that moment instead.
                    appOpenPending = appOpen != null
                }
            }
            else -> Unit
        }
    }

    private val activityTracker = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
            if (appOpenPending) {
                appOpenPending = false
                appOpen?.show(activity)
            }
            if (!reviewRequested) {
                reviewRequested = true
                reviewPrompter.maybeRequest(activity)
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (currentActivity === activity) currentActivity = null
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}
