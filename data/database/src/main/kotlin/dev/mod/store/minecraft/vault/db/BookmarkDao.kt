package dev.mod.store.minecraft.data.database.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface BookmarkDao {

    @Query("SELECT COUNT(*) FROM bookmark")
    fun observeCount(): Flow<Int>

    @Query("SELECT creationId FROM bookmark ORDER BY addedAt DESC")
    suspend fun selectIds(): List<Int>

    @Query("SELECT creationId FROM bookmark ORDER BY addedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun selectPage(limit: Int, offset: Int): List<Int>

    @Query("SELECT COUNT(*) FROM bookmark WHERE creationId = :creationId")
    suspend fun countFor(creationId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkDbo)

    @Query("DELETE FROM bookmark WHERE creationId = :creationId")
    suspend fun delete(creationId: Int)

    @Query("DELETE FROM bookmark")
    suspend fun deleteAll()
}
