package dev.mod.store.minecraft.data.database.config

import com.russhwolf.settings.Settings
import dev.mod.store.minecraft.domain.config.AdChance
import dev.mod.store.minecraft.domain.config.AdToggles
import dev.mod.store.minecraft.domain.config.ConfigCache
import dev.mod.store.minecraft.domain.config.ConfigEntity
import dev.mod.store.minecraft.domain.config.NativeKind

/**
 * Persists the last fetched config to multiplatform-settings so a flaky network call can fall
 * back to the previous values instead of the all-disabled defaults.
 */
internal class ConfigCacheDataSource(
    private val settings: Settings,
) : ConfigCache {

    override fun read(): ConfigEntity = ConfigEntity(
        adToggles = AdToggles(
            appOpen = settings.getBoolean(KEY_OPEN_ENABLED, false),
            native = settings.getBoolean(KEY_NATIVE_ENABLED, false),
            interstitial = settings.getBoolean(KEY_INTER_ENABLED, false),
        ),
        adChance = AdChance(
            appOpen = settings.getInt(KEY_OPEN_CHANCE, 100),
            native = settings.getInt(KEY_NATIVE_CHANCE, 100),
            interstitial = settings.getInt(KEY_INTER_CHANCE, 100),
        ),
        interstitialCooldownSeconds = settings.getInt(KEY_COOLDOWN, 60),
        nativePreloadSize = settings.getInt(KEY_PRELOAD, 3),
        nativeInterval = settings.getInt(KEY_INTERVAL, 3),
        nativeKind = NativeKind.fromRaw(settings.getStringOrNull(KEY_NATIVE_KIND)),
    )

    override fun write(config: ConfigEntity) {
        settings.putBoolean(KEY_OPEN_ENABLED, config.adToggles.appOpen)
        settings.putBoolean(KEY_NATIVE_ENABLED, config.adToggles.native)
        settings.putBoolean(KEY_INTER_ENABLED, config.adToggles.interstitial)
        settings.putInt(KEY_OPEN_CHANCE, config.adChance.appOpen)
        settings.putInt(KEY_NATIVE_CHANCE, config.adChance.native)
        settings.putInt(KEY_INTER_CHANCE, config.adChance.interstitial)
        settings.putInt(KEY_COOLDOWN, config.interstitialCooldownSeconds)
        settings.putInt(KEY_PRELOAD, config.nativePreloadSize)
        settings.putInt(KEY_INTERVAL, config.nativeInterval)
        settings.putString(KEY_NATIVE_KIND, config.nativeKind.name)
    }

    private companion object {
        const val KEY_OPEN_ENABLED = "cfg.open.enabled"
        const val KEY_NATIVE_ENABLED = "cfg.native.enabled"
        const val KEY_INTER_ENABLED = "cfg.inter.enabled"
        const val KEY_OPEN_CHANCE = "cfg.open.chance"
        const val KEY_NATIVE_CHANCE = "cfg.native.chance"
        const val KEY_INTER_CHANCE = "cfg.inter.chance"
        const val KEY_COOLDOWN = "cfg.cooldown"
        const val KEY_PRELOAD = "cfg.preload"
        const val KEY_INTERVAL = "cfg.interval"
        const val KEY_NATIVE_KIND = "cfg.native.kind"
    }
}
