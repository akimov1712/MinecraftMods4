package dev.mod.store.minecraft.data.database.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import dev.mod.store.minecraft.domain.bookmark.BookmarkRepository
import dev.mod.store.minecraft.domain.config.ConfigCache
import dev.mod.store.minecraft.domain.loadout.LoadoutRepository
import dev.mod.store.minecraft.domain.review.ReviewTracker
import dev.mod.store.minecraft.data.database.bookmark.BookmarkLocalDataSource
import dev.mod.store.minecraft.data.database.bookmark.BookmarkRepositoryImpl
import dev.mod.store.minecraft.data.database.config.ConfigCacheDataSource
import dev.mod.store.minecraft.data.database.db.VaultDatabase
import dev.mod.store.minecraft.data.database.db.createVaultDatabase
import dev.mod.store.minecraft.data.database.loadout.LoadoutLocalDataSource
import dev.mod.store.minecraft.data.database.loadout.LoadoutRepositoryImpl
import dev.mod.store.minecraft.data.database.review.ReviewTrackerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val SETTINGS_NAME = "vault_settings"

/** Local persistence graph: SQLDelight database + multiplatform-settings, and their repos. */
val vaultModule = module {
    single<VaultDatabase> { createVaultDatabase(androidContext()) }
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE),
        )
    }

    single { BookmarkLocalDataSource(get()) }
    single { LoadoutLocalDataSource(androidContext(), get()) }

    single<BookmarkRepository> { BookmarkRepositoryImpl(get()) }
    single<LoadoutRepository> { LoadoutRepositoryImpl(get()) }
    single<ConfigCache> { ConfigCacheDataSource(get()) }
    single<ReviewTracker> { ReviewTrackerImpl(get()) }
}
