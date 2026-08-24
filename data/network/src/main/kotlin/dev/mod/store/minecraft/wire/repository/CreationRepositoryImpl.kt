package dev.mod.store.minecraft.data.network.repository

import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationPage
import dev.mod.store.minecraft.domain.creation.CreationQuery
import dev.mod.store.minecraft.domain.creation.CreationRepository
import dev.mod.store.minecraft.core.common.error.AppError
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.data.network.mapper.toEntity
import dev.mod.store.minecraft.data.network.network.networkCall
import dev.mod.store.minecraft.data.network.source.CatalogRemoteDataSource

internal class CreationRepositoryImpl(
    private val remote: CatalogRemoteDataSource,
) : CreationRepository {

    override suspend fun fetchCatalog(query: CreationQuery): Outcome<CreationPage> =
        networkCall {
            val response = remote.fetchCatalog(query)
            CreationPage(
                items = response.items.map { it.toEntity() },
                total = response.total,
            )
        }

    override suspend fun fetchCreation(id: Int): Outcome<CreationEntity> =
        networkCall { remote.fetchCreation(id).toEntity() }

    override suspend fun fetchPickOfDay(): Outcome<CreationEntity> =
        networkCall { remote.fetchPickOfDay().toEntity() }

    override suspend fun fetchFileSize(url: String): Outcome<Long> =
        when (val outcome = networkCall { remote.fetchFileSize(url) }) {
            is Outcome.Done ->
                outcome.value?.takeIf { it > 0L }
                    ?.let { Outcome.Done(it) }
                    ?: Outcome.Failed(AppError.NetworkError.NOT_FOUND)

            is Outcome.Failed -> Outcome.Failed(outcome.error)
        }
}
