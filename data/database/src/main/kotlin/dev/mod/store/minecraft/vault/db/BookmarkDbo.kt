package dev.mod.store.minecraft.data.database.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A bookmarked creation. Presence of a row means it's bookmarked. */
@Entity(tableName = "bookmark")
internal data class BookmarkDbo(
    @PrimaryKey val creationId: Int,
    val addedAt: Long,
)
