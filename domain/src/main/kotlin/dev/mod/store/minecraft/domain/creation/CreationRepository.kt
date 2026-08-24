package dev.mod.store.minecraft.domain.creation

import dev.mod.store.minecraft.core.common.outcome.Outcome

/** Read access to the remote catalog of creations. Implemented in :wire over Ktor. */
interface CreationRepository {
    suspend fun fetchCatalog(query: CreationQuery): Outcome<CreationPage>
    suspend fun fetchCreation(id: Int): Outcome<CreationEntity>

    /** The creation the backend features today — identical for every user until midnight. */
    suspend fun fetchPickOfDay(): Outcome<CreationEntity>

    suspend fun fetchFileSize(url: String): Outcome<Long>
}
