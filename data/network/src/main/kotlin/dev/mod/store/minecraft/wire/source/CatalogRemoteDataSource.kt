package dev.mod.store.minecraft.data.network.source

import dev.mod.store.minecraft.domain.creation.CreationFeed
import dev.mod.store.minecraft.domain.creation.CreationQuery
import dev.mod.store.minecraft.data.network.BuildConfig
import dev.mod.store.minecraft.data.network.dto.CreationDto
import dev.mod.store.minecraft.data.network.dto.CreationListResponseDto
import dev.mod.store.minecraft.data.network.dto.DownloadCountDto
import dev.mod.store.minecraft.data.network.network.currentLanguageTag
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
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

    /**
     * `appId` is what makes the server fill in `trendingPosition` and `similarMods`; without it
     * both come back empty, even for a mod that sits near the top of the trending list.
     */
    suspend fun fetchCreation(id: Int): CreationDto =
        client.get("${BuildConfig.BASE_URL}/v1/mod/$id") {
            header("Language", currentLanguageTag())
            parameter("appId", BuildConfig.APP_ID)
        }.body()

    /** Counts one download of [modId] from this app; the server answers with the new total. */
    suspend fun recordDownload(modId: Int): DownloadCountDto =
        client.post("$appCatalog/$modId/download").body()

    /** The backend's daily pick — the same creation for every user until the date rolls over. */
    suspend fun fetchPickOfDay(): CreationDto =
        client.get("$appCatalog/day") {
            header("Language", currentLanguageTag())
        }.body()

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
        CreationFeed.Fresh -> "new"
        else -> "actived"
    }

private val CreationFeed.sortKey: String?
    get() = when (this) {
        CreationFeed.Trending -> "order"
        CreationFeed.Popular -> "usedCount"
        CreationFeed.TopRated -> "rating"
        CreationFeed.Fresh -> null
    }

private val CreationFeed.sortValue: String
    get() = when (this) {
        CreationFeed.Trending -> "asc"
        else -> "desc"
    }
