package dev.mod.store.minecraft.feature.walkthrough

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class WalkthroughStep(
    val number: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val textRes: Int,
    @param:DrawableRes val image: Int?,
)

internal val walkthroughSteps: List<WalkthroughStep> = listOf(
    WalkthroughStep(1, R.string.walkthrough_s1_title, R.string.walkthrough_s1_text, R.drawable.walkthrough_step_1),
    WalkthroughStep(2, R.string.walkthrough_s2_title, R.string.walkthrough_s2_text, R.drawable.walkthrough_step_2),
    WalkthroughStep(3, R.string.walkthrough_s3_title, R.string.walkthrough_s3_text, R.drawable.walkthrough_step_3),
    WalkthroughStep(4, R.string.walkthrough_s4_title, R.string.walkthrough_s4_text, R.drawable.walkthrough_step_4),
    WalkthroughStep(5, R.string.walkthrough_s5_title, R.string.walkthrough_s5_text, R.drawable.walkthrough_step_5),
    WalkthroughStep(6, R.string.walkthrough_s6_title, R.string.walkthrough_s6_text, R.drawable.walkthrough_step_6),
    WalkthroughStep(7, R.string.walkthrough_s7_title, R.string.walkthrough_s7_text, null),
)
