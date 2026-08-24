package dev.mod.store.minecraft.core.ads.internal

import android.app.Application
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import io.github.aakira.napier.Napier

/** AppMetrica bring-up + a tiny event reporter. No-ops when the API key is absent. */
internal class Analytics {

    private var active = false

    fun start(application: Application, apiKey: String) {
        if (apiKey.isBlank()) {
            Napier.w("METRICA_API_KEY is blank — analytics skipped", tag = "billboard")
            return
        }
        val config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionTimeout(60)
            .withLogs()
            .build()
        AppMetrica.activate(application, config)
        AppMetrica.enableActivityAutoTracking(application)
        active = true
    }

    fun report(event: String, attrs: Map<String, Any?> = emptyMap()) {
        if (!active) return
        if (attrs.isEmpty()) AppMetrica.reportEvent(event) else AppMetrica.reportEvent(event, attrs)
    }
}
