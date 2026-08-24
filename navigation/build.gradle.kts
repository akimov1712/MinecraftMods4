plugins {
    id("minecraftmods.android.library")
    id("minecraftmods.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.mod.store.minecraft.navigation"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(project(":core:ui"))

    // The shell is the only place that knows about every section and stitches them together.
    implementation(project(":feature:ignition"))
    implementation(project(":feature:hub"))
    implementation(project(":feature:showcase"))
    implementation(project(":feature:spotlight"))
    implementation(project(":feature:loadout"))
    implementation(project(":feature:stash"))
    implementation(project(":feature:outreach"))
    implementation(project(":feature:compendium"))
    implementation(project(":feature:walkthrough"))

    implementation(libs.bundles.koin)
    implementation(libs.bundles.decompose)
    implementation(libs.bundles.mvikotlin)
    implementation(libs.mvikotlin.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.napier)
}
