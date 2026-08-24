package dev.mod.store.minecraft.data.network.repository

import dev.mod.store.minecraft.domain.outreach.OutreachRepository
import dev.mod.store.minecraft.domain.outreach.RecommendationEntity
import dev.mod.store.minecraft.domain.outreach.ReportEntity
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.data.network.mapper.toDto
import dev.mod.store.minecraft.data.network.network.networkCall
import dev.mod.store.minecraft.data.network.source.OutreachRemoteDataSource

internal class OutreachRepositoryImpl(
    private val remote: OutreachRemoteDataSource,
) : OutreachRepository {

    override suspend fun submitReport(report: ReportEntity): Outcome<Unit> =
        networkCall { remote.submitReport(report.toDto()) }

    override suspend fun submitRecommendation(recommendation: RecommendationEntity): Outcome<Unit> =
        networkCall { remote.submitRecommendation(recommendation.toDto()) }
}
