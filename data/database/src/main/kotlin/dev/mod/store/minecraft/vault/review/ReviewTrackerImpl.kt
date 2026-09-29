package dev.mod.store.minecraft.data.database.review

import com.russhwolf.settings.Settings
import dev.mod.store.minecraft.domain.review.ReviewTracker

internal class ReviewTrackerImpl(
    private val settings: Settings,
) : ReviewTracker {

    override var promptAlreadyShown: Boolean
        get() = settings.getBoolean(KEY_SHOWN, false)
        set(value) = settings.putBoolean(KEY_SHOWN, value)

    private companion object {
        const val KEY_SHOWN = "review.shown"
    }
}
