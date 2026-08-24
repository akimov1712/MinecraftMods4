package dev.mod.store.minecraft.domain.config

/**
 * Local persistence of the last known good [ConfigEntity]. [read] always returns a usable value
 * (sane defaults before anything is cached). Implemented in :vault over multiplatform-settings.
 */
interface ConfigCache {
    fun read(): ConfigEntity
    fun write(config: ConfigEntity)
}
