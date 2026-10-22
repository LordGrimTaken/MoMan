plugins {
    alias(momanx.plugins.android.library)
    alias(momanx.plugins.spotless)

    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "moman.core.archive"
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.archive)
    implementation(libs.unifile)
}
