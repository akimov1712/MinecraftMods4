package dev.mod.store.minecraft.data.database.db

import android.content.Context
import androidx.room.Room

private const val DATABASE_NAME = "vault.db"

/** Builds the Room database. */
internal fun createVaultDatabase(context: Context): VaultDatabase =
    Room.databaseBuilder(
        context.applicationContext,
        VaultDatabase::class.java,
        DATABASE_NAME,
    ).build()
