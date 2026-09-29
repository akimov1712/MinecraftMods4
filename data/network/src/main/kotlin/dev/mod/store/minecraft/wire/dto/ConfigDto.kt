package dev.mod.store.minecraft.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ConfigResponseDto(
    @SerialName("sdk") val config: ConfigDto? = null,
    @SerialName("ads") val ads: List<AdPlacementDto>? = null,
)

/** One entry of the response's `ads` array: a native slot and the panel's switch for it. */
@Serializable
internal data class AdPlacementDto(
    @SerialName("adId") val adId: String? = null,
    @SerialName("label") val label: String? = null,
    @SerialName("isEnabled") val isEnabled: Boolean? = null,
)

@Serializable
internal data class ConfigDto(
    @SerialName("isOpenAdsEnabled") val isOpenAdsEnabled: Boolean = false,
    @SerialName("isNativeAdsEnabled") val isNativeAdsEnabled: Boolean = false,
    @SerialName("isInterAdsEnabled") val isInterAdsEnabled: Boolean = false,
    @SerialName("delayInter") val delayInter: Int = 60,
    @SerialName("countNativePreload") val countNativePreload: Int = 3,
    // Note: backend key keeps the historical "Inverval" typo.
    @SerialName("adsInverval") val adsInterval: Int = 3,
    @SerialName("adsNativeType") val adsNativeType: String? = null,
    @SerialName("chanceShowOpenAds") val chanceShowOpenAds: Int = 100,
    @SerialName("chanceShowNativeAds") val chanceShowNativeAds: Int = 100,
    @SerialName("chanceShowInterAds") val chanceShowInterAds: Int = 100,
)
