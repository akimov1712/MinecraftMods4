package dev.mod.store.minecraft.core.ads.internal

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import dev.mod.store.minecraft.domain.review.ReviewTracker
import io.github.aakira.napier.Napier

/**
 * Runs the Play in-app review flow when the caller says the reader has just had something go right,
 * at most once per install.
 *
 * Play decides whether the form appears at all — there is a quota, and the API never says either
 * way. So the install is marked as asked only once Play has actually run the flow; a failed request
 * leaves the next download free to try again.
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

        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (!request.isSuccessful) {
                Napier.w("requestReviewFlow failed: ${request.exception}", tag = TAG)
                return@addOnCompleteListener
            }
            manager.launchReviewFlow(activity, request.result).addOnCompleteListener { flow ->
                if (flow.isSuccessful) {
                    tracker.promptAlreadyShown = true
                    Napier.d("review flow completed", tag = TAG)
                } else {
                    Napier.w("launchReviewFlow failed: ${flow.exception}", tag = TAG)
                }
            }
        }
    }

    private companion object {
        const val TAG = "billboard"
    }
}
