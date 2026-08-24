package dev.mod.store.minecraft.domain.config

import dev.mod.store.minecraft.core.common.outcome.Outcome

/** Fetches the monetization configuration. Network impl in :wire; caching wraps it in :vault. */
interface ConfigRepository {
    suspend fun fetchConfig(): Outcome<ConfigEntity>
}
