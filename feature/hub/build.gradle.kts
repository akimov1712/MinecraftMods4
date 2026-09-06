plugins {
    id("minecraftmods.android.library")
    id("minecraftmods.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.mod.store.minecraft.feature.hub"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(project(":core:ui"))
    // The hub is the tab aggregator, so it composes its tab sections directly.
    implementation(project(":feature:showcase"))
    implementation(project(":feature:stash"))
    implementation(project(":feature:outreach"))
    implementation(project(":feature:compendium"))
    implementation(project(":feature:settings"))

    implementation(libs.bundles.koin)
    implementation(libs.bundles.decompose)
    implementation(libs.bundles.mvikotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.napier)
}
