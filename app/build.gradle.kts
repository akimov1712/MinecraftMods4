plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.cas)
}

val appArtifactIdValue: String = providers.gradleProperty("appArtifactId").getOrElse("app")
val appApplicationId: String = "dev.modstore.$appArtifactIdValue"

cas {
    casId = appApplicationId
    includeOptimalAds = true
    adapters {
        ironSource = true
        googleAds = true
        unityAds = true
        kidoz = true
        liftoffMonetize = true
        inMobi = true
        chartboost = true
        dtExchange = true
        mintegral = true
        appLovin = true
        audienceNetwork = true
        pangle = true
        yangoAds = true
        bigoAds = true
        casExchange = true
        startIO = true
        hyprMX = true
        ysoNetwork = true
        ogury = true
        prado = true
        superAwesome = true
        smaato = true
        maticoo = true
    }
}

val appVersionCodeValue: Int = providers.gradleProperty("appVersionCode").map(String::toInt).getOrElse(1)
val appVersionNameValue: String = providers.gradleProperty("appVersionName").getOrElse("1.0")
val metricaKey: String = providers.gradleProperty("metricaApiKey").getOrElse("")

android {
    namespace = "dev.mod.store.minecraft"
    compileSdk = 36

    defaultConfig {
        applicationId = appApplicationId
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCodeValue
        versionName = appVersionNameValue

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "METRICA_API_KEY", "\"$metricaKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":navigation"))
    implementation(project(":core:common"))
    implementation(project(":domain"))
    implementation(project(":core:ui"))
    implementation(project(":data:network"))
    implementation(project(":data:database"))
    implementation(project(":core:ads"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.bundles.koin)
    implementation(libs.bundles.decompose)
    implementation(libs.bundles.mvikotlin)
    implementation(libs.mvikotlin.logging)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.napier)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
