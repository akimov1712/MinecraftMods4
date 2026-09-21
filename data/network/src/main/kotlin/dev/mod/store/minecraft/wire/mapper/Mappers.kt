package dev.mod.store.minecraft.data.network.mapper

import dev.mod.store.minecraft.domain.config.AdChance
import dev.mod.store.minecraft.domain.config.AdToggles
import dev.mod.store.minecraft.domain.config.ConfigEntity
import dev.mod.store.minecraft.domain.config.NativeKind
import dev.mod.store.minecraft.domain.creation.CreationCategory
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.outreach.RecommendationEntity
import dev.mod.store.minecraft.domain.reaction.ReactionSummary
import dev.mod.store.minecraft.domain.reaction.ReactionType
import dev.mod.store.minecraft.domain.outreach.ReportEntity
import dev.mod.store.minecraft.data.network.dto.ConfigDto
import dev.mod.store.minecraft.data.network.dto.CreationDto
import dev.mod.store.minecraft.data.network.dto.ReactionsDto
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
    // Positions arrive 1-based; anything else is the server saying "not in the selection".
    trendingPosition = trendingPosition?.takeIf { it > 0 },
    downloadsCount = downloadsCount.coerceAtLeast(0),
    similar = similar.filter { it.id != id }.map { it.toEntity() },
)

/** Reaction names this build does not know are dropped rather than failing the whole summary. */
internal fun ReactionsDto.toEntity(): ReactionSummary = ReactionSummary(
    selected = selected?.toReactionType(),
    counts = counts.mapNotNull { (name, count) ->
        name.toReactionType()?.let { it to count.coerceAtLeast(0) }
    }.toMap(),
    total = total.coerceAtLeast(0),
)

private fun String.toReactionType(): ReactionType? =
    ReactionType.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }

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
