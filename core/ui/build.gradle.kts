plugins {
    id("minecraftmods.android.library")
    id("minecraftmods.android.compose")
}

android {
    namespace = "dev.mod.store.minecraft.core.ui"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))

    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.landscapist.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.telephoto.zoomable.image)
    implementation(libs.napier)
}
