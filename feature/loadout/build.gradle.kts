plugins {
    id("minecraftmods.android.library")
    id("minecraftmods.android.compose")
}

android {
    namespace = "dev.mod.store.minecraft.feature.loadout"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(project(":core:ui"))
    implementation(project(":core:ads"))

    implementation(libs.bundles.koin)
    implementation(libs.bundles.decompose)
    implementation(libs.bundles.mvikotlin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.napier)
}
