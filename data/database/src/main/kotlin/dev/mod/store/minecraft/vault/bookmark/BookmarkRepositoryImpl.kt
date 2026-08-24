package dev.mod.store.minecraft.data.database.bookmark

import dev.mod.store.minecraft.domain.bookmark.BookmarkRepository
import kotlinx.coroutines.flow.Flow

internal class BookmarkRepositoryImpl(
    private val local: BookmarkLocalDataSource,
) : BookmarkRepository {

    override fun observeBookmarkCount(): Flow<Int> = local.observeCount()

    override suspend fun toggle(creationId: Int): Boolean = local.toggle(creationId)

    override suspend fun isBookmarked(creationId: Int): Boolean = local.isBookmarked(creationId)

    override suspend fun bookmarkedIds(): List<Int> = local.bookmarkedIds()

    override suspend fun bookmarkedPage(limit: Int, offset: Int): List<Int> =
        local.page(limit, offset)
}
