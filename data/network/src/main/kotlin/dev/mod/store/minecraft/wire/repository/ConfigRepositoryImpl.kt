package dev.mod.store.minecraft.data.network.repository

import dev.mod.store.minecraft.domain.config.ConfigEntity
import dev.mod.store.minecraft.domain.config.ConfigRepository
import dev.mod.store.minecraft.core.common.error.AppError
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.data.network.mapper.toEntity
import dev.mod.store.minecraft.data.network.network.networkCall
import dev.mod.store.minecraft.data.network.source.ConfigRemoteDataSource
import kotlinx.coroutines.delay

/**
 * Network config with a small linear backoff: one flaky call would otherwise drop the whole
 * session onto the all-ads-disabled defaults, so a few seconds of retries is worth it.
 */
internal class ConfigRepositoryImpl(
    private val remote: ConfigRemoteDataSource,
) : ConfigRepository {

    override suspend fun fetchConfig(): Outcome<ConfigEntity> {
        var lastError: AppError = AppError.NetworkError.UNKNOWN
        repeat(MAX_ATTEMPTS) { attempt ->
            when (val outcome = networkCall { remote.fetchConfig() }) {
                is Outcome.Done -> {
                    val config = outcome.value.config
                    if (config != null) return Outcome.Done(config.toEntity())
                    lastError = AppError.NetworkError.SERVER
                }

                is Outcome.Failed -> lastError = outcome.error
            }
            if (attempt < MAX_ATTEMPTS - 1) {
                delay(BACKOFF_MS * (attempt + 1))
            }
        }
        return Outcome.Failed(lastError)
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
        const val BACKOFF_MS = 800L
    }
}
