package dev.mod.store.minecraft.domain.outreach

import dev.mod.store.minecraft.core.common.outcome.Outcome

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
