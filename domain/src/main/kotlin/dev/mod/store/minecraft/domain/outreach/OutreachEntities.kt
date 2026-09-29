package dev.mod.store.minecraft.domain.outreach

import dev.mod.store.minecraft.core.common.outcome.Outcome

/**
 * The address every submission is filed under while the app does not ask for one.
 *
 * The forms have no email field for now, so there is nothing to put in an entity's `email`; this
 * stands in for it. The use cases still take the address as a parameter, so restoring the field is
 * a change at the two call sites and nowhere else.
 */
const val FEEDBACK_EMAIL = "default@gmail.com"

/** A user-submitted bug report / feedback message. */
data class ReportEntity(
    val email: String,
    val message: String,
)

/** A user's suggestion for a creation to add to the catalog. */
data class RecommendationEntity(
    val email: String,
    val message: String,
)

/** Outbound user submissions. Implemented in :wire. */
interface OutreachRepository {
    suspend fun submitReport(report: ReportEntity): Outcome<Unit>
    suspend fun submitRecommendation(recommendation: RecommendationEntity): Outcome<Unit>
}
