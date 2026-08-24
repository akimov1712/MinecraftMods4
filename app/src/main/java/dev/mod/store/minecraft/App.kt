package dev.mod.store.minecraft

import android.app.Application
import dev.mod.store.minecraft.core.ads.BillboardController
import dev.mod.store.minecraft.di.appKoinModules
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appKoinModules)
        }
        get<BillboardController>().start()
    }
}
