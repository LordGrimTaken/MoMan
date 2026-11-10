buildscript {
    dependencies {
        classpath(libs.kotlin.gradle)
    }
}

plugins {
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineProfile) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.metro) apply false
    alias(libs.plugins.moko.resources) apply false
    alias(libs.plugins.sqldelight) apply false

    alias(mihonx.plugins.spotless)
}

val buildLogic: IncludedBuild = gradle.includedBuild("build-logic")
tasks {
    listOf("clean", "spotlessApply", "spotlessCheck").forEach { task ->
        named(task) {
            dependsOn(buildLogic.task(":$task"))
        }
    }
}

/**
 * Fails when an AndroidManifest names an app class that does not exist.
 *
 * A rename moved classes once and left two manifest strings pointing at the old package; the app crashed on launch
 * and no build step noticed (see tools/check-manifest-classes.py). This makes that check a Gradle task so it runs
 * with the rest of verification instead of relying on someone remembering the script exists.
 */
tasks.register<Exec>("checkManifestClasses") {
    group = "verification"
    description = "Verify every app-owned android:name in the manifests resolves to a real class"
    workingDir = layout.projectDirectory.asFile
    commandLine("python3", "tools/check-manifest-classes.py")
}

/**
 * Fails when a translation's format placeholders disagree with the base string.
 *
 * A locale that drops `%s` silently loses the variable the code passes (the category name vanishes from
 * "Delete this category?"); an invented placeholder can throw. Neither is visible to the compiler. Run with
 * `./gradlew checkTranslations` alongside `checkManifestClasses`.
 */
tasks.register<Exec>("checkTranslations") {
    group = "verification"
    description = "Verify translations keep the base string's format placeholders"
    workingDir = layout.projectDirectory.asFile
    commandLine("python3", "tools/check-translations.py")
}

/**
 * Fails when a manifest exports a component that is not on the reviewed allowlist in
 * tools/check-exported-components.py. Exported components are reachable by any app on the device.
 */
tasks.register<Exec>("checkExportedComponents") {
    group = "verification"
    description = "Verify every exported component without a permission is a reviewed decision"
    workingDir = layout.projectDirectory.asFile
    commandLine("python3", "tools/check-exported-components.py")
}
