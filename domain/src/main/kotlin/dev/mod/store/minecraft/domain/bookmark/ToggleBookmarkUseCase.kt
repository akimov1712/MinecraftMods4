package dev.mod.store.minecraft.domain.bookmark

/** Flips the bookmark state of a creation and returns the new value. */
class ToggleBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(creationId: Int): Boolean = bookmarkRepository.toggle(creationId)
}
