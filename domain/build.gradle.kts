plugins {
    id("minecraftmods.kotlin.jvm")
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
