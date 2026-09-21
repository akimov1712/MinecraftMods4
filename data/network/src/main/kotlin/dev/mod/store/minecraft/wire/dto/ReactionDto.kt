package dev.mod.store.minecraft.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReactionsDto(
    @SerialName("selected") val selected: String? = null,
    @SerialName("counts") val counts: Map<String, Int> = emptyMap(),
    @SerialName("total") val total: Int = 0,
)

/**
 * The write body. [reaction] has no default on purpose: a property without a default is always
 * encoded, so clearing a reaction sends an explicit `{"reaction":null}` — which is what the backend
 * reads as "remove" — rather than an empty object.
 */
@Serializable
internal data class SetReactionDto(
    @SerialName("reaction") val reaction: String?,
)
