package dev.mod.store.minecraft.data.network.source

import dev.mod.store.minecraft.domain.creation.CreationFeed
import dev.mod.store.minecraft.domain.creation.CreationQuery
import dev.mod.store.minecraft.data.network.BuildConfig
import dev.mod.store.minecraft.data.network.dto.CreationDto
import dev.mod.store.minecraft.data.network.dto.CreationListResponseDto
import dev.mod.store.minecraft.data.network.network.currentLanguageTag
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.contentLength

/** Raw HTTP access to the catalog endpoints. Returns DTOs; mapping/wrapping happens upstream. */
internal class CatalogRemoteDataSource(private val client: HttpClient) {

    private val appCatalog = "${BuildConfig.BASE_URL}/v1/apps/${BuildConfig.APP_ID}/mod"

    suspend fun fetchCatalog(query: CreationQuery): CreationListResponseDto =
        client.get("$appCatalog/${query.feed.pathSegment}") {
            header("Language", currentLanguageTag())
            parameter("q", query.searchText)
            query.category?.let { parameter("category", it.filterValue) }
            query.feed.sortKey?.let { key ->
                parameter("sort_key", key)
                parameter("sort_value", query.feed.sortValue)
            }
            parameter("skip", query.offset)
            parameter("take", query.limit)
        }.body()

    suspend fun fetchCreation(id: Int): CreationDto =
        client.get("${BuildConfig.BASE_URL}/v1/mod/$id") {
            header("Language", currentLanguageTag())
        }.body()

    /** The backend's daily pick — the same creation for every user until the date rolls over. */
    suspend fun fetchPickOfDay(): CreationDto =
        client.get("$appCatalog/inactived") {
            header("Language", currentLanguageTag())
            parameter("skip", 3)
            parameter("take", 1)
            parameter("sort_key", "rating")
            parameter("sort_value", "desc")
        }.body<CreationListResponseDto>().items.first()

    /** Content-Length probe via HEAD; null when the server omits the header. */
    suspend fun fetchFileSize(url: String): Long? =
        client.head(url).contentLength()
}

/**
 * Backend shape of a feed. "Fresh" is its own endpoint (already ordered by publication date);
 * the rest are the active catalog re-sorted, and `sort_key` only accepts `order`, `usedCount`
 * and `rating`.
 */
private val CreationFeed.pathSegment: String
    get() = when (this) {
        CreationFeed.Fresh -> "inactived"
        else -> "inactived"
    }

private val CreationFeed.sortKey: String?
    get() = when (this) {
        CreationFeed.Trending -> "rating"
        CreationFeed.Popular -> "usedCount"
        CreationFeed.TopRated -> "rating"
        CreationFeed.Fresh -> null
    }

private val CreationFeed.sortValue: String
    get() = when (this) {
        CreationFeed.Trending -> "asc"
        else -> "desc"
    }
