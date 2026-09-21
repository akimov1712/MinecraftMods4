package dev.mod.store.minecraft.feature.walkthrough

import dev.mod.store.minecraft.core.ui.R as Shared
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal data class WalkthroughStep(
    val number: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val textRes: Int,
    @param:DrawableRes val image: Int?,
)

internal val walkthroughSteps: List<WalkthroughStep> = listOf(
    WalkthroughStep(1, Shared.string.walkthrough_s1_title, Shared.string.walkthrough_s1_text, R.drawable.walkthrough_step_1),
    WalkthroughStep(2, Shared.string.walkthrough_s2_title, Shared.string.walkthrough_s2_text, R.drawable.walkthrough_step_2),
    WalkthroughStep(3, Shared.string.walkthrough_s3_title, Shared.string.walkthrough_s3_text, R.drawable.walkthrough_step_3),
    WalkthroughStep(4, Shared.string.walkthrough_s4_title, Shared.string.walkthrough_s4_text, R.drawable.walkthrough_step_4),
    WalkthroughStep(5, Shared.string.walkthrough_s5_title, Shared.string.walkthrough_s5_text, R.drawable.walkthrough_step_5),
    WalkthroughStep(6, Shared.string.walkthrough_s6_title, Shared.string.walkthrough_s6_text, R.drawable.walkthrough_step_6),
    WalkthroughStep(7, Shared.string.walkthrough_s7_title, Shared.string.walkthrough_s7_text, null),
)
