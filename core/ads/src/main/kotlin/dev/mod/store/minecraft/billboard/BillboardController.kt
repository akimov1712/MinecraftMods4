package dev.mod.store.minecraft.core.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import dev.mod.store.minecraft.core.ads.internal.Analytics
import dev.mod.store.minecraft.core.ads.internal.ReviewPrompter
import dev.mod.store.minecraft.domain.config.AdPlacement
import dev.mod.store.minecraft.domain.config.FetchConfigUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Boots the process-wide services once from the Application and keeps hold of whichever Activity is
 * in front, so callers (Decompose components) never need to carry one.
 *
 * This build ships without advertising. The ad SDK is no longer a dependency, so the [ScreenAds]
 * answers below are all "nothing to show" and no slot anywhere fills. What is still real: analytics
 * start-up, the remote configuration fetch (cached for the next launch, and it still carries every
 * ad switch for when they come back) and the Play review prompt.
 */
class BillboardController internal constructor(
    private val application: Application,
    private val fetchConfig: FetchConfigUseCase,
    private val reviewPrompter: ReviewPrompter,
    private val analytics: Analytics,
    private val config: BillboardConfig,
) : ScreenAds, ReviewPrompt {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val booted = CompletableDeferred<Unit>()

    private var currentActivity: Activity? = null
    private var started = false

    override val nativeInterval: Int get() = 0

    override fun hasNativeAd(placement: AdPlacement): Boolean = false

    override fun nativeAllowed(placement: AdPlacement): Boolean = false

    override suspend fun awaitBoot() = booted.await()

    override fun onSpotlightEntered() = Unit

    override fun onFileSaved() {
        currentActivity?.let { reviewPrompter.maybeRequest(it) }
    }

    fun start() {
        if (started) return
        started = true

        analytics.start(application, config.metricaKey)
        application.registerActivityLifecycleCallbacks(activityTracker)

        scope.launch {
            try {
                // Nothing on screen depends on it today, but it keeps the cache current so a
                // launch without network still has the last known configuration.
                fetchConfig()
            } finally {
                // The splash gate waits on this, so it has to be released whatever happened.
                booted.complete(Unit)
            }
        }
    }

    private val activityTracker = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            currentActivity = activity
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
