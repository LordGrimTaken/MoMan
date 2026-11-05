import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import com.moman.gradle.configurations.configureKotlin
import com.moman.gradle.extensions.alias
import com.moman.gradle.extensions.configureTest
import com.moman.gradle.extensions.coreLibraryDesugaring
import com.moman.gradle.extensions.libs
import com.moman.gradle.extensions.mihonx
import com.moman.gradle.extensions.plugins
import com.moman.gradle.extensions.release
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

@Suppress("UNUSED")
class PluginKotlinMultiplatform : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            alias(libs.plugins.android.kmp.library)
            alias(libs.plugins.kotlin.multiplatform)
        }

        configureKotlin()
        configureTest()

        kotlin {
            @OptIn(ExperimentalKotlinGradlePluginApi::class)
            applyDefaultHierarchyTemplate()

            android {
                minSdk = mihonx.versions.android.sdk.min.get().toInt()
                compileSdk {
                    version = release(mihonx.versions.android.sdk.compile)
                }
                enableCoreLibraryDesugaring = true
            }
        }

        dependencies {
            coreLibraryDesugaring(libs.android.desugar)
        }
    }
}

private fun Project.kotlin(block: KotlinMultiplatformExtension.() -> Unit) {
    extensions.configure(block)
}

private fun KotlinMultiplatformExtension.android(block: KotlinMultiplatformAndroidLibraryTarget.() -> Unit) {
    targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach(block)
}
