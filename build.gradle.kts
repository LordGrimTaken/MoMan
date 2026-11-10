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
