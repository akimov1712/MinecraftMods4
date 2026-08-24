package dev.mod.store.minecraft.di

import dev.mod.store.minecraft.core.ads.di.billboardModule
import dev.mod.store.minecraft.navigation.di.shellModule
import dev.mod.store.minecraft.data.database.di.vaultModule
import dev.mod.store.minecraft.data.network.di.wireModule
import org.koin.core.module.Module

/** Single registry of every Koin module the app boots with. */
val appKoinModules: List<Module> = listOf(
    platformModule,
    domainModule,
    wireModule,
    vaultModule,
    billboardModule,
    shellModule,
)
