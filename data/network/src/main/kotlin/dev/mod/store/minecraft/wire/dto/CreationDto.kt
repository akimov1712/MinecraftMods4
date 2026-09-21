package dev.mod.store.minecraft.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreationDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("image") val imageUrl: String,
    @SerialName("category") val category: String,
    @SerialName("descriptionImages") val gallery: List<String> = emptyList(),
    @SerialName("files") val files: List<String> = emptyList(),
    @SerialName("versions") val versions: List<VersionDto> = emptyList(),
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("commentCounts") val commentCount: Int = 0,
    @SerialName("reactionsCount") val reactionCount: Int = 0,
    @SerialName("createdAt") val createdAt: String = "",
    /** 1-based place in the app's trending selection; null outside it. Needs `appId` on the request. */
    @SerialName("trendingPosition") val trendingPosition: Int? = null,
    /** Other mods from the same app, never this one. Needs `appId` on the request. */
    @SerialName("similarMods") val similar: List<CreationDto> = emptyList(),
    /** Not in the responses yet; read as soon as the backend starts sending it. */
    @SerialName("downloadsCount") val downloadsCount: Int = 0,
)

@Serializable
internal data class VersionDto(
    @SerialName("version") val version: String,
)

@Serializable
internal data class CreationListResponseDto(
    @SerialName("count") val total: Int = 0,
    @SerialName("mods") val items: List<CreationDto> = emptyList(),
)

@Serializable
internal data class DownloadCountDto(
    @SerialName("downloadsCount") val downloadsCount: Int = 0,
)
