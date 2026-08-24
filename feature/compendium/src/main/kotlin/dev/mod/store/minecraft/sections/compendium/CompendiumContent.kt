package dev.mod.store.minecraft.feature.compendium

import androidx.annotation.StringRes

internal data class FaqEntry(
    val id: String,
    @param:StringRes val questionRes: Int,
    @param:StringRes val answerRes: Int,
)

internal data class FaqGroup(
    @param:StringRes val titleRes: Int,
    val entries: List<FaqEntry>,
)

internal val faqGroups: List<FaqGroup> = listOf(
    FaqGroup(
        titleRes = R.string.faq_group_start,
        entries = listOf(
            FaqEntry("what", R.string.faq_what_q, R.string.faq_what_a),
            FaqEntry("internet", R.string.faq_internet_q, R.string.faq_internet_a),
        ),
    ),
    FaqGroup(
        titleRes = R.string.faq_group_install,
        entries = listOf(
            FaqEntry("how", R.string.faq_how_q, R.string.faq_how_a),
            FaqEntry("fail", R.string.faq_fail_q, R.string.faq_fail_a),
            FaqEntry("stuck", R.string.faq_stuck_q, R.string.faq_stuck_a),
        ),
    ),
    FaqGroup(
        titleRes = R.string.faq_group_ingame,
        entries = listOf(
            FaqEntry("hidden", R.string.faq_hidden_q, R.string.faq_hidden_a),
            FaqEntry("lag", R.string.faq_lag_q, R.string.faq_lag_a),
        ),
    ),
    FaqGroup(
        titleRes = R.string.faq_group_app,
        entries = listOf(
            FaqEntry("ads", R.string.faq_ads_q, R.string.faq_ads_a),
            FaqEntry("free", R.string.faq_free_q, R.string.faq_free_a),
        ),
    ),
    FaqGroup(
        titleRes = R.string.faq_group_content,
        entries = listOf(
            FaqEntry("fresh", R.string.faq_fresh_q, R.string.faq_fresh_a),
            FaqEntry("safe", R.string.faq_safe_q, R.string.faq_safe_a),
        ),
    ),
)
