package dev.mod.store.minecraft.domain.config

import dev.mod.store.minecraft.core.common.outcome.Outcome

/**
 * Resolves the monetization config: network first (with the repo's own retries), persisting a
 * successful result to the cache, and falling back to the last cached value otherwise. Always
 * returns a usable [ConfigEntity] — callers never have to handle failure.
 */
class FetchConfigUseCase(
    private val configRepository: ConfigRepository,
    private val configCache: ConfigCache,
) {
    suspend operator fun invoke(): ConfigEntity =
        when (val outcome = configRepository.fetchConfig()) {
            is Outcome.Done -> outcome.value.also(configCache::write)
            is Outcome.Failed -> configCache.read()
        }
}
