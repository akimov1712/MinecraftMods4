package dev.mod.store.minecraft.domain.creation

import dev.mod.store.minecraft.domain.bookmark.BookmarkRepository
import dev.mod.store.minecraft.core.common.outcome.Outcome
import dev.mod.store.minecraft.core.common.outcome.map

/**
 * One page of the catalog with each creation's bookmark state merged in. The feature layer only
 * ever talks to this — it never sees the network or the bookmark store directly.
 */
class FetchShowcaseUseCase(
    private val creationRepository: CreationRepository,
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(query: CreationQuery): Outcome<CreationPage> {
        val outcome = creationRepository.fetchCatalog(query)
        val bookmarked = bookmarkRepository.bookmarkedIds().toHashSet()
        return outcome.map { page -> page.copy(items = page.items.markBookmarked(bookmarked)) }
    }
}

/** Flags every creation whose id is in [bookmarked]. */
internal fun List<CreationEntity>.markBookmarked(bookmarked: Set<Int>): List<CreationEntity> =
    map { creation -> creation.copy(isBookmarked = creation.id in bookmarked) }
