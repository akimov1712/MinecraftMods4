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
    title = title.unescapeHtml(),
    description = description.unescapeHtml(),
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

/**
 * Descriptions are pasted from web pages, so they arrive as fragments of HTML. Dropping the tags
 * and turning the handful of entities that actually occur back into characters keeps things like
 * `<figure>` and `-&gt;` from reaching the screen.
 */
private val HTML_TAG = Regex("<[^>]+>")
private val BLANK_LINES = Regex("\n{3,}")

private fun String.unescapeHtml(): String = this
    .replace(HTML_TAG, "")
    .replace("&nbsp;", " ")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&apos;", "'")
    .replace("&rsquo;", "\u2019")
    .replace("&lsquo;", "\u2018")
    .replace("&ldquo;", "\u201C")
    .replace("&rdquo;", "\u201D")
    .replace("&ndash;", "\u2013")
    .replace("&mdash;", "\u2014")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&amp;", "&")
    .replace(BLANK_LINES, "\n\n")
    .trim()

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
