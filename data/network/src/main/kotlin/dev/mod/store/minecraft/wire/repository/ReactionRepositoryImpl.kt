package dev.mod.store.minecraft.data.network.repository

import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.data.network.mapper.toEntity
import dev.mod.store.minecraft.data.network.network.networkCall
import dev.mod.store.minecraft.data.network.source.ReactionRemoteDataSource
import dev.mod.store.minecraft.domain.identity.ClientIdentity
import dev.mod.store.minecraft.domain.reaction.ReactionRepository
import dev.mod.store.minecraft.domain.reaction.ReactionSummary
import dev.mod.store.minecraft.domain.reaction.ReactionType

internal class ReactionRepositoryImpl(
    private val remote: ReactionRemoteDataSource,
    private val identity: ClientIdentity,
) : ReactionRepository {

    override suspend fun fetch(modId: Int): Outcome<ReactionSummary> =
        networkCall { remote.fetch(modId, identity.id).toEntity() }

    override suspend fun set(modId: Int, reaction: ReactionType?): Outcome<Unit> =
        networkCall { remote.set(modId, identity.id, reaction?.name) }
}
