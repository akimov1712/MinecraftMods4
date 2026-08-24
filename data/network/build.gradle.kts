plugins {
    id("minecraftmods.android.library")
    alias(libs.plugins.kotlin.serialization)
}

val backendBaseUrl: String = providers.gradleProperty("BASE_URL").orNull
    ?: error("BASE_URL is missing from gradle.properties")
val backendAppId: String = providers.gradleProperty("APP_ID").orNull
    ?: error("APP_ID is missing from gradle.properties")

android {
    namespace = "dev.mod.store.minecraft.data.network"

    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"$backendBaseUrl\"")
        buildConfigField("int", "APP_ID", backendAppId)
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":domain"))

    implementation(libs.bundles.ktor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.koin.core)
    implementation(libs.napier)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
