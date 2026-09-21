package dev.mod.store.minecraft.data.network.di

import dev.mod.store.minecraft.domain.config.ConfigRepository
import dev.mod.store.minecraft.domain.creation.CreationRepository
import dev.mod.store.minecraft.domain.outreach.OutreachRepository
import dev.mod.store.minecraft.domain.reaction.ReactionRepository
import dev.mod.store.minecraft.data.network.network.buildHttpClient
import dev.mod.store.minecraft.data.network.repository.ConfigRepositoryImpl
import dev.mod.store.minecraft.data.network.repository.CreationRepositoryImpl
import dev.mod.store.minecraft.data.network.repository.OutreachRepositoryImpl
import dev.mod.store.minecraft.data.network.repository.ReactionRepositoryImpl
import dev.mod.store.minecraft.data.network.source.CatalogRemoteDataSource
import dev.mod.store.minecraft.data.network.source.ConfigRemoteDataSource
import dev.mod.store.minecraft.data.network.source.OutreachRemoteDataSource
import dev.mod.store.minecraft.data.network.source.ReactionRemoteDataSource
import io.ktor.client.HttpClient
import org.koin.dsl.module

/** Networking graph: one shared Ktor client → data sources → repository implementations. */
val wireModule = module {
    single<HttpClient> { buildHttpClient() }

    single { CatalogRemoteDataSource(get()) }
    single { ConfigRemoteDataSource(get()) }
    single { OutreachRemoteDataSource(get()) }
    single { ReactionRemoteDataSource(get()) }

    single<CreationRepository> { CreationRepositoryImpl(get()) }
    single<ConfigRepository> { ConfigRepositoryImpl(get()) }
    single<OutreachRepository> { OutreachRepositoryImpl(get()) }
    single<ReactionRepository> { ReactionRepositoryImpl(get(), get()) }
}
