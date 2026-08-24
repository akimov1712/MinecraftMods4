package dev.mod.store.minecraft.core.ads.internal

import android.content.Context
import com.cleversolutions.ads.AdType
import com.cleversolutions.ads.android.CAS
import dev.mod.store.minecraft.domain.config.ConfigEntity
import io.github.aakira.napier.Napier

/**
 * Blocking CAS SDK bring-up — must run off the main thread. Ad types requested mirror what the
 * server config has enabled, so disabled placements never even initialize.
 */
internal fun Context.initializeCas(casId: String, config: ConfigEntity, debug: Boolean) {
    CAS.settings.debugMode = debug

    CAS.buildManager()
        .withCasId(casId)
        .withTestAdMode(debug)
        .withAdTypes(*config.enabledAdTypes())
        .withCompletionListener { result ->
            Napier.d(
                "CAS init · sdk=${CAS.getSDKVersion()} · err=${result.error} · country=${result.countryCode}",
                tag = "billboard",
            )
        }
        .build(this)
}

private fun ConfigEntity.enabledAdTypes(): Array<AdType> = buildList {
    if (adToggles.interstitial) add(AdType.Interstitial)
    if (adToggles.appOpen) add(AdType.AppOpen)
    if (adToggles.native) {
        add(AdType.Native)
        add(AdType.Banner)
    }
}.toTypedArray()
