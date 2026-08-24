package dev.mod.store.minecraft.data.network.mapper

import dev.mod.store.minecraft.domain.config.AdChance
import dev.mod.store.minecraft.domain.config.AdToggles
import dev.mod.store.minecraft.domain.config.ConfigEntity
import dev.mod.store.minecraft.domain.config.NativeKind
import dev.mod.store.minecraft.domain.creation.CreationCategory
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.outreach.RecommendationEntity
import dev.mod.store.minecraft.domain.outreach.ReportEntity
import dev.mod.store.minecraft.data.network.dto.ConfigDto
import dev.mod.store.minecraft.data.network.dto.CreationDto
import dev.mod.store.minecraft.data.network.dto.RecommendationDto
import dev.mod.store.minecraft.data.network.dto.ReportDto
import java.time.Instant

internal fun CreationDto.toEntity(isBookmarked: Boolean = false): CreationEntity = CreationEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    category = CreationCategory.fromResponse(category),
    gallery = gallery,
    fileUrls = files,
    supportedVersions = versions.map { it.version },
    rating = rating,
    commentCount = commentCount,
    reactionCount = reactionCount,
    publishedAtEpochMs = createdAt.toEpochMillisOrNull(),
    isBookmarked = isBookmarked,
)

/** Parses the ISO-8601 timestamps the backend sends; unparseable or absent values become null. */
private fun String.toEpochMillisOrNull(): Long? =
    takeIf { it.isNotBlank() }?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }

internal fun ConfigDto.toEntity(): ConfigEntity = ConfigEntity(
    adToggles = AdToggles(
        appOpen = isOpenAdsEnabled,
        native = isNativeAdsEnabled,
        interstitial = isInterAdsEnabled,
    ),
    adChance = AdChance(
        appOpen = chanceShowOpenAds,
        native = chanceShowNativeAds,
        interstitial = chanceShowInterAds,
    ),
    interstitialCooldownSeconds = delayInter,
    nativePreloadSize = countNativePreload,
    nativeInterval = adsInterval,
    nativeKind = NativeKind.fromRaw(adsNativeType),
)

internal fun ReportEntity.toDto(): ReportDto = ReportDto(message = message, email = email)

internal fun RecommendationEntity.toDto(): RecommendationDto =
    RecommendationDto(email = email, message = message)
