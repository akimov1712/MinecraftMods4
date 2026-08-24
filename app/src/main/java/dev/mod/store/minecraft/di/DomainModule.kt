package dev.mod.store.minecraft.di

import dev.mod.store.minecraft.domain.bookmark.FetchBookmarkedCreationsUseCase
import dev.mod.store.minecraft.domain.bookmark.FetchBookmarkedIdsUseCase
import dev.mod.store.minecraft.domain.bookmark.ObserveBookmarkCountUseCase
import dev.mod.store.minecraft.domain.bookmark.ToggleBookmarkUseCase
import dev.mod.store.minecraft.domain.config.FetchConfigUseCase
import dev.mod.store.minecraft.domain.creation.FetchCreationUseCase
import dev.mod.store.minecraft.domain.creation.FetchFileSizeUseCase
import dev.mod.store.minecraft.domain.creation.FetchHomeDigestUseCase
import dev.mod.store.minecraft.domain.creation.FetchShowcaseUseCase
import dev.mod.store.minecraft.domain.loadout.DownloadCreationUseCase
import dev.mod.store.minecraft.domain.loadout.IsCreationDownloadedUseCase
import dev.mod.store.minecraft.domain.loadout.OpenCreationFileUseCase
import dev.mod.store.minecraft.domain.outreach.SubmitRecommendationUseCase
import dev.mod.store.minecraft.domain.outreach.SubmitReportUseCase
import org.koin.dsl.module

/** Use cases — pure-Kotlin orchestration over the repositories bound in :wire / :vault. */
val domainModule = module {
    single { FetchShowcaseUseCase(get(), get()) }
    single { FetchHomeDigestUseCase(get(), get()) }
    single { FetchCreationUseCase(get(), get()) }
    single { FetchFileSizeUseCase(get()) }
    single { ToggleBookmarkUseCase(get()) }
    single { ObserveBookmarkCountUseCase(get()) }
    single { FetchBookmarkedCreationsUseCase(get(), get()) }
    single { FetchBookmarkedIdsUseCase(get()) }
    single { SubmitReportUseCase(get()) }
    single { SubmitRecommendationUseCase(get()) }
    single { FetchConfigUseCase(get(), get()) }
    single { DownloadCreationUseCase(get()) }
    single { IsCreationDownloadedUseCase(get()) }
    single { OpenCreationFileUseCase(get()) }
}
