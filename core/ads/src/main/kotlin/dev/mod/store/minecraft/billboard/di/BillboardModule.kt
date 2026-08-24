package dev.mod.store.minecraft.core.ads.di

import dev.mod.store.minecraft.core.ads.BillboardController
import dev.mod.store.minecraft.core.ads.BillboardConfig
import dev.mod.store.minecraft.core.ads.ScreenAds
import dev.mod.store.minecraft.core.ads.internal.Analytics
import dev.mod.store.minecraft.core.ads.internal.ReviewPrompter
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Monetization graph. [BillboardConfig] is supplied by :app (it needs BuildConfig). The
 * controller is exposed both as itself (so the Application can [BillboardController.start] it)
 * and as [ScreenAds] (so sections can request fullscreen ads).
 */
val billboardModule = module {
    single { Analytics() }
    single { ReviewPrompter(androidContext(), get(), get<BillboardConfig>().debug) }
    single {
        BillboardController(
            application = androidApplication(),
            fetchConfig = get(),
            reviewPrompter = get(),
            analytics = get(),
            config = get(),
        )
    } bind ScreenAds::class
}
