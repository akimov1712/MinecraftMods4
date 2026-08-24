package dev.mod.store.minecraft.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.mod.store.minecraft.BuildConfig
import dev.mod.store.minecraft.core.ui.state.AndroidFaultMessages
import dev.mod.store.minecraft.core.ui.state.FaultMessages
import dev.mod.store.minecraft.core.ads.BillboardConfig
import dev.mod.store.minecraft.core.common.concurrency.DispatcherProvider
import dev.mod.store.minecraft.core.common.concurrency.StandardDispatcherProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Cross-cutting singletons that need the application context or build flavour to be created.
 * Everything feature-specific lives in its own module and is aggregated in [appKoinModules].
 */
val platformModule = module {
    single<StoreFactory> {
        if (BuildConfig.DEBUG) LoggingStoreFactory(DefaultStoreFactory()) else DefaultStoreFactory()
    }
    single<DispatcherProvider> { StandardDispatcherProvider() }
    single<FaultMessages> { AndroidFaultMessages(androidContext()) }
    single {
        BillboardConfig(
            casId = androidContext().packageName,
            metricaKey = BuildConfig.METRICA_API_KEY,
            debug = BuildConfig.DEBUG,
        )
    }
}
