plugins {
    `kotlin-dsl`
}

group = "dev.mod.store.minecraft.buildlogic"

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "minecraftmods.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "minecraftmods.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("kotlinJvm") {
            id = "minecraftmods.kotlin.jvm"
            implementationClass = "KotlinJvmConventionPlugin"
        }
    }
}
