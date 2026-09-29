package dev.mod.store.minecraft.core.ads.di

import dev.mod.store.minecraft.core.ads.BillboardController
import dev.mod.store.minecraft.core.ads.BillboardConfig
import dev.mod.store.minecraft.core.ads.ReviewPrompt
import dev.mod.store.minecraft.core.ads.ScreenAds
import dev.mod.store.minecraft.core.ads.internal.Analytics
import dev.mod.store.minecraft.core.ads.internal.ReviewPrompter
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.binds
import org.koin.dsl.module

/**
 * Process-wide services. [BillboardConfig] is supplied by :app (it needs BuildConfig). The
 * controller is exposed as itself (so the Application can [BillboardController.start] it), as
 * [ScreenAds] and as [ReviewPrompt] (so the downloads screen can ask for a rating).
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
    } binds arrayOf(ScreenAds::class, ReviewPrompt::class)
}
