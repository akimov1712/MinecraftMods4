package dev.mod.store.minecraft.navigation.di

import org.koin.dsl.module

/**
 * Shell-level wiring. Empty for now — the root navigation component is created by the activity
 * (it needs a ComponentContext, so it can't be a plain singleton). As sections gain their own
 * Koin modules this is where the shell will pull them together.
 */
val shellModule = module {
}
