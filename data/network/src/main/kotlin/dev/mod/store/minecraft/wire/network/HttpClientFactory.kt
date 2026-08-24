package dev.mod.store.minecraft.data.network.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val TIMEOUT_MS = 60_000L

/**
 * Builds the single shared Ktor client (OkHttp engine). `expectSuccess = true` turns non-2xx
 * responses into exceptions so [networkCall] can translate them uniformly into [AppError].
 */
internal fun buildHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            },
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = TIMEOUT_MS
        connectTimeoutMillis = TIMEOUT_MS
        socketTimeoutMillis = TIMEOUT_MS
    }

    install(Logging) {
        logger = NapierKtorLogger
        level = LogLevel.INFO
    }
}

private object NapierKtorLogger : io.ktor.client.plugins.logging.Logger {
    override fun log(message: String) {
        Napier.d(message = message, tag = "Ktor")
    }
}
