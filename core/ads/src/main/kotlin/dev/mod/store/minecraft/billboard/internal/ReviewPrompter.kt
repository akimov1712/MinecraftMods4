package dev.mod.store.minecraft.core.ads.internal

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import dev.mod.store.minecraft.domain.review.ReviewTracker
import io.github.aakira.napier.Napier

private val TRIGGER_LAUNCHES = setOf(3, 7, 15)

/**
 * Drives the Play in-app review flow on the [TRIGGER_LAUNCHES]th cold launches, at most once per
 * install. Launch counting is delegated to [ReviewTracker] (persisted in :vault).
 */
internal class ReviewPrompter(
    context: Context,
    private val tracker: ReviewTracker,
    debug: Boolean,
) {
    private val manager =
        if (debug) FakeReviewManager(context) else ReviewManagerFactory.create(context)

    fun maybeRequest(activity: Activity) {
        if (tracker.promptAlreadyShown) return
        val launches = tracker.recordLaunch()
        if (launches !in TRIGGER_LAUNCHES) return

        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Napier.w("requestReviewFlow failed: ${task.exception}", tag = "billboard")
                return@addOnCompleteListener
            }
            manager.launchReviewFlow(activity, task.result).addOnCompleteListener {
                tracker.promptAlreadyShown = true
            }
        }
    }
}
