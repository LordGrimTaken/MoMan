import com.moman.gradle.extensions.alias
import com.moman.gradle.extensions.libs
import com.moman.gradle.extensions.mihonx
import com.moman.gradle.extensions.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UNUSED")
class PluginAndroidTest : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            alias(libs.plugins.android.test)
            alias(mihonx.plugins.android.base)
        }
    }
}
