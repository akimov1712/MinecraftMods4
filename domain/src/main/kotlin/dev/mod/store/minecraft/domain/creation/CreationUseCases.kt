package dev.mod.store.minecraft.domain.creation

import dev.mod.store.minecraft.domain.bookmark.BookmarkRepository
import dev.mod.store.minecraft.core.common.outcome.Outcome

/** Loads a single creation by id with its bookmark state merged in. */
class FetchCreationUseCase(
    private val creationRepository: CreationRepository,
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(creationId: Int): Outcome<CreationEntity> =
        when (val outcome = creationRepository.fetchCreation(creationId)) {
            is Outcome.Done -> Outcome.Done(
                outcome.value.copy(isBookmarked = bookmarkRepository.isBookmarked(creationId)),
            )
            is Outcome.Failed -> outcome
        }
}

/** Resolves the byte size of a downloadable file (HEAD request). */
class FetchFileSizeUseCase(
    private val creationRepository: CreationRepository,
) {
    suspend operator fun invoke(url: String): Outcome<Long> = creationRepository.fetchFileSize(url)
}
