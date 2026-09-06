package dev.mod.store.minecraft.data.database.bookmark

import dev.mod.store.minecraft.data.database.db.BookmarkDbo
import dev.mod.store.minecraft.data.database.db.VaultDatabase
import kotlinx.coroutines.flow.Flow

/** Room-backed access to the bookmark table. Room suspend/Flow queries are already main-safe. */
internal class BookmarkLocalDataSource(database: VaultDatabase) {

    private val dao = database.bookmarkDao()

    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun bookmarkedIds(): List<Int> = dao.selectIds()

    suspend fun page(limit: Int, offset: Int): List<Int> = dao.selectPage(limit, offset)

    suspend fun isBookmarked(creationId: Int): Boolean = dao.countFor(creationId) > 0

    suspend fun clear() = dao.deleteAll()

    /** Inserts when absent, deletes when present. Returns the new bookmarked state. */
    suspend fun toggle(creationId: Int): Boolean {
        val present = dao.countFor(creationId) > 0
        return if (present) {
            dao.delete(creationId)
            false
        } else {
            dao.insert(BookmarkDbo(creationId = creationId, addedAt = System.currentTimeMillis()))
            true
        }
    }
}
