package dev.mod.store.minecraft.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReportDto(
    @SerialName("text") val message: String,
    @SerialName("email") val email: String,
)

@Serializable
internal data class RecommendationDto(
    @SerialName("email") val email: String,
    @SerialName("description") val message: String,
)
