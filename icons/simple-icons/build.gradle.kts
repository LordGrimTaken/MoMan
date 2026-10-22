plugins {
    alias(momanx.plugins.android.library)
    alias(momanx.plugins.compose)

    alias(momanx.plugins.spotless)

    alias(libs.plugins.valkyrie)
}

android {
    namespace = "moman.icons.simpleicons"
}

spotless {
    format("svg") {
        target("src/**/*.svg")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

valkyrie {
    packageName = "moman.icons.simpleicons"
    generateAtSync = true

    imageVector {
        suppressUnusedReceiverWarning = true
    }

    iconPack {
        name = "SimpleIcons"
        targetSourceSet = "main"
    }
}

dependencies {
    api(libs.androidx.compose.ui)
}
