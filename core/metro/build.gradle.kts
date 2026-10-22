plugins {
    alias(momanx.plugins.android.library)
    alias(momanx.plugins.spotless)

    alias(libs.plugins.metro)
}

android {
    namespace = "moman.core.metro"
}

dependencies {
    implementation(libs.metro.runtime)
}
