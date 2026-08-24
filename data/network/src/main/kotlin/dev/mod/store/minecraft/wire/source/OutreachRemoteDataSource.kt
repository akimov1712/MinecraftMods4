package dev.mod.store.minecraft.data.network.source

import dev.mod.store.minecraft.data.network.BuildConfig
import dev.mod.store.minecraft.data.network.dto.RecommendationDto
import dev.mod.store.minecraft.data.network.dto.ReportDto
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/** Raw HTTP access to the outbound submission endpoints. */
internal class OutreachRemoteDataSource(private val client: HttpClient) {

    suspend fun submitReport(dto: ReportDto) {
        client.post("${BuildConfig.BASE_URL}/v1/apps/${BuildConfig.APP_ID}/issue") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
    }

    suspend fun submitRecommendation(dto: RecommendationDto) {
        client.post("${BuildConfig.BASE_URL}/v1/apps/${BuildConfig.APP_ID}/mod/recommend") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }
    }
}
