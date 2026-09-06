package dev.mod.store.minecraft.domain.bookmark

import kotlinx.coroutines.flow.Flow

/**
 * Local bookmarks. A creation is bookmarked iff a row exists for it (presence-based, no flag),
 * so [toggle] inserts or deletes. Implemented in :vault over SQLDelight.
 */
interface BookmarkRepository {
    fun observeBookmarkCount(): Flow<Int>
    suspend fun toggle(creationId: Int): Boolean
    suspend fun isBookmarked(creationId: Int): Boolean
    suspend fun bookmarkedIds(): List<Int>
    suspend fun bookmarkedPage(limit: Int, offset: Int): List<Int>

    /** Forgets every bookmark — the "clear saved" action in settings. */
    suspend fun clear()
}
