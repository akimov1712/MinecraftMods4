package dev.mod.store.minecraft.domain.creation

import dev.mod.store.minecraft.domain.bookmark.BookmarkRepository
import dev.mod.store.minecraft.core.common.error.AppError
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.core.common.outcome.valueOrNull
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** How many creations each home rail asks for. */
private const val RAIL_SIZE = 10

/**
 * Builds the whole home screen in one call: the pick of the day plus one page of every rail,
 * all fired in parallel and each degrading to "absent" on its own failure. The call only fails
 * when every slice failed, so a missing pick of the day (an app with no mods attached, say)
 * still leaves a usable screen.
 */
class FetchHomeDigestUseCase(
    private val creationRepository: CreationRepository,
    private val bookmarkRepository: BookmarkRepository,
) {

    suspend operator fun invoke(): Outcome<HomeDigest> = coroutineScope {
        val pickOfDayAsync = async { creationRepository.fetchPickOfDay() }
        val railsAsync = CreationFeed.entries.map { feed -> feed to async { rail(feed) } }

        val pickOfDay = pickOfDayAsync.await()
        val rails = railsAsync.associate { (feed, request) -> feed to request.await() }
        val outcomes: List<Outcome<*>> = listOf(pickOfDay) + rails.values

        if (outcomes.all { it is Outcome.Failed }) {
            val error = outcomes.firstNotNullOfOrNull { (it as? Outcome.Failed)?.error }
            return@coroutineScope Outcome.Failed(error ?: AppError.NetworkError.UNKNOWN)
        }

        val bookmarked = bookmarkRepository.bookmarkedIds().toHashSet()
        fun items(feed: CreationFeed): List<CreationEntity> =
            rails[feed]?.valueOrNull()?.items.orEmpty().markBookmarked(bookmarked)

        Outcome.Done(
            HomeDigest(
                pickOfDay = pickOfDay.valueOrNull()
                    ?.let { pick -> pick.copy(isBookmarked = pick.id in bookmarked) },
                trending = items(CreationFeed.Trending),
                popular = items(CreationFeed.Popular),
                topRated = items(CreationFeed.TopRated),
                fresh = items(CreationFeed.Fresh),
                catalogTotal = rails.values.maxOf { it.valueOrNull()?.total ?: 0 },
            ),
        )
    }

    private suspend fun rail(feed: CreationFeed): Outcome<CreationPage> =
        creationRepository.fetchCatalog(CreationQuery(feed = feed, limit = RAIL_SIZE))
}
