plugins {
    alias(momanx.plugins.android.library)
    alias(momanx.plugins.spotless)

    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "tachiyomi.core.metadata"
}

dependencies {
    implementation(projects.sourceApi)

    implementation(libs.bundles.serialization)
}
