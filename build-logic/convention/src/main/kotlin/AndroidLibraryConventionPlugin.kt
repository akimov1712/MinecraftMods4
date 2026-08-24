import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Baseline for every Android library module. AGP 9 ships built-in Kotlin support, so the
 * standalone `kotlin.android` plugin is intentionally NOT applied here — applying
 * `com.android.library` is enough to compile Kotlin sources.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = BuildTargets.COMPILE_SDK
            defaultConfig {
                minSdk = BuildTargets.MIN_SDK
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}
