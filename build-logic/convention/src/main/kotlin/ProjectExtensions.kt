import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/** Shared toolchain levels so every module agrees on the same SDK / JVM target. */
object BuildTargets {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
}

/** Convenience accessor for the `libs` version catalog from inside a convention plugin. */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
