package dev.mod.store.minecraft.domain.bookmark

import dev.mod.store.minecraft.domain.creation.CreationEntity
import dev.mod.store.minecraft.domain.creation.CreationRepository
import dev.mod.store.minecraft.core.common.outcome.Outcome
import kotlinx.coroutines.flow.Flow

/** Live count of bookmarks — drives the stash header and triggers reloads when it changes. */
class ObserveBookmarkCountUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(): Flow<Int> = bookmarkRepository.observeBookmarkCount()
}

/** One page of bookmarked creations, newest first, each fetched fresh and flagged bookmarked. */
class FetchBookmarkedCreationsUseCase(
    private val bookmarkRepository: BookmarkRepository,
    private val creationRepository: CreationRepository,
) {
    suspend operator fun invoke(offset: Int, limit: Int): Outcome<List<CreationEntity>> {
        val ids = bookmarkRepository.bookmarkedPage(limit = limit, offset = offset)
        val creations = ArrayList<CreationEntity>(ids.size)
        for (id in ids) {
            when (val outcome = creationRepository.fetchCreation(id)) {
                is Outcome.Done -> creations += outcome.value.copy(isBookmarked = true)
                is Outcome.Failed -> return Outcome.Failed(outcome.error)
            }
        }
        return Outcome.Done(creations)
    }
}

/** Ids of every bookmarked creation — lets a loaded list re-flag itself without refetching. */
class FetchBookmarkedIdsUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(): Set<Int> = bookmarkRepository.bookmarkedIds().toHashSet()
}
