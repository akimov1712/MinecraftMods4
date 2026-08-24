package dev.mod.store.minecraft.data.database.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [BookmarkDbo::class], version = 1, exportSchema = false)
internal abstract class VaultDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}
