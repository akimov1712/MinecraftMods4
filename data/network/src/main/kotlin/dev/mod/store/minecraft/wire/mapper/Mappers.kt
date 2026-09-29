package dev.mod.store.minecraft.data.network.mapper

import dev.mod.store.minecraft.domain.config.AdChance
import dev.mod.store.minecraft.domain.config.AdPlacement
import dev.mod.store.minecraft.domain.config.AdToggles
import dev.mod.store.minecraft.domain.config.ConfigEntity
import dev.mod.store.minecraft.domain.config.NativeKind
import dev.mod.store.minecraft.domain.creation.CreationCategory
import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.outreach.RecommendationEntity
import dev.mod.store.minecraft.domain.reaction.ReactionSummary
import dev.mod.store.minecraft.domain.reaction.ReactionType
import dev.mod.store.minecraft.domain.outreach.ReportEntity
import dev.mod.store.minecraft.data.network.dto.AdPlacementDto
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

/**
 * @param placements the response's `ads` array. Null — an older server, or no list configured at
 * all — leaves every slot enabled rather than silently blanking the app's ads.
 */
internal fun ConfigDto.toEntity(placements: List<AdPlacementDto>? = null): ConfigEntity = ConfigEntity(
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
    enabledPlacements = placements.toEnabledPlacements(),
)

private fun List<AdPlacementDto>?.toEnabledPlacements(): Set<AdPlacement> {
    if (this == null) return AdPlacement.entries.toSet()
    val known = mapNotNull { dto -> AdPlacement.fromId(dto.adId)?.to(dto.isEnabled ?: true) }
    // A slot the panel has not heard of yet is left on, so shipping a new placement does not
    // require a backend change first.
    val unmentioned = AdPlacement.entries - known.map { it.first }.toSet()
    return (known.filter { it.second }.map { it.first } + unmentioned).toSet()
}

internal fun ReportEntity.toDto(): ReportDto = ReportDto(message = message, email = email)

internal fun RecommendationEntity.toDto(): RecommendationDto =
    RecommendationDto(email = email, message = message)
