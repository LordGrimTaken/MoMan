import com.diffplug.gradle.spotless.SpotlessExtension
import moman.gradle.extensions.alias
import moman.gradle.extensions.libs
import moman.gradle.extensions.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

@Suppress("UNUSED")
class PluginSpotless : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        plugins {
            alias(libs.plugins.spotless)
        }

        // Configuration should be synced with [/gradle/build-logic/build.gradle.kts]
        val ktlintVersion = libs.ktlint.bom.get().version
        spotless {
            kotlin {
                target("src/**/*.kt")
                ktlint(ktlintVersion)
                trimTrailingWhitespace()
                endWithNewline()
                forbidRegex(
                    "materialSymbolsRImport",
                    "(?m)^import moman\\.icons\\.materialsymbols\\.R(?! as MaterialSymbolsR$)\\b.*$",
                    "Import moman.icons.materialsymbols.R as MaterialSymbolsR",
                )
                forbidRegex(
                    "materialSymbolsRQualified",
                    "(?m)^(?!import ).*\\bmoman\\.icons\\.materialsymbols\\.R\\.",
                    "Use the MaterialSymbolsR import alias instead of the qualified name",
                )
                forbidRegex(
                    "customMaterialSymbolsRImport",
                    "(?m)^import moman\\.icons\\.custommaterialsymbols\\.R(?! as CustomMaterialSymbolsR$)\\b.*$",
                    "Import moman.icons.custommaterialsymbols.R as CustomMaterialSymbolsR",
                )
                forbidRegex(
                    "customMaterialSymbolsRQualified",
                    "(?m)^(?!import ).*\\bmoman\\.icons\\.custommaterialsymbols\\.R\\.",
                    "Use the CustomMaterialSymbolsR import alias instead of the qualified name",
                )
            }

            kotlinGradle {
                target("*.kts")
                ktlint(ktlintVersion)
                trimTrailingWhitespace()
                endWithNewline()
            }

            format("xml") {
                target("src/**/*.xml")
                trimTrailingWhitespace()
                endWithNewline()
            }
        }
    }
}

private fun Project.spotless(block: SpotlessExtension.() -> Unit) {
    extensions.configure(block)
}
