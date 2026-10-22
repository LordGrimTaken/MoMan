import moman.gradle.extensions.alias
import moman.gradle.extensions.libs
import moman.gradle.extensions.momanx
import moman.gradle.extensions.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UNUSED")
class PluginAndroidTest : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            alias(libs.plugins.android.test)
            alias(momanx.plugins.android.base)
        }
    }
}
