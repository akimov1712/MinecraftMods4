package dev.mod.store.minecraft.feature.compendium

import androidx.annotation.StringRes

/** One question the assistant knows how to answer. */
data class FaqEntry(
    val id: String,
    @param:StringRes val questionRes: Int,
    @param:StringRes val answerRes: Int,
    /** Shown on the chip — a short form of the question, so the chip row stays readable. */
    @param:StringRes val chipRes: Int,
)

/**
 * The whole script, flat and ordered by how often it comes up. There are no categories: the
 * assistant offers whatever has not been asked yet.
 */
val faqEntries: List<FaqEntry> = listOf(
    FaqEntry("how", R.string.faq_how_q, R.string.faq_how_a, R.string.faq_how_chip),
    FaqEntry("fail", R.string.faq_fail_q, R.string.faq_fail_a, R.string.faq_fail_chip),
    FaqEntry("stuck", R.string.faq_stuck_q, R.string.faq_stuck_a, R.string.faq_stuck_chip),
    FaqEntry("hidden", R.string.faq_hidden_q, R.string.faq_hidden_a, R.string.faq_hidden_chip),
    FaqEntry("lag", R.string.faq_lag_q, R.string.faq_lag_a, R.string.faq_lag_chip),
    FaqEntry("what", R.string.faq_what_q, R.string.faq_what_a, R.string.faq_what_chip),
    FaqEntry("internet", R.string.faq_internet_q, R.string.faq_internet_a, R.string.faq_internet_chip),
    FaqEntry("free", R.string.faq_free_q, R.string.faq_free_a, R.string.faq_free_chip),
    FaqEntry("ads", R.string.faq_ads_q, R.string.faq_ads_a, R.string.faq_ads_chip),
    FaqEntry("fresh", R.string.faq_fresh_q, R.string.faq_fresh_a, R.string.faq_fresh_chip),
    FaqEntry("safe", R.string.faq_safe_q, R.string.faq_safe_a, R.string.faq_safe_chip),
)
