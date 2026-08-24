package dev.mod.store.minecraft.data.network.source

import dev.mod.store.minecraft.data.network.BuildConfig
import dev.mod.store.minecraft.data.network.dto.ConfigResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/** Raw HTTP access to the app-configuration endpoint. */
internal class ConfigRemoteDataSource(private val client: HttpClient) {

    suspend fun fetchConfig(): ConfigResponseDto =
        client.get("${BuildConfig.BASE_URL}/v1/apps/${BuildConfig.APP_ID}").body()
}
