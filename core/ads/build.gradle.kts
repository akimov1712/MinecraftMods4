plugins {
    id("minecraftmods.android.library")
    id("minecraftmods.android.compose")
}

android {
    namespace = "dev.mod.store.minecraft.core.ads"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(project(":core:ui"))

    api(libs.cas.sdk)
    implementation(libs.appmetrica.analytics)
    implementation(libs.play.review.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.napier)
}
