plugins {
    alias(momanx.plugins.android.library)
    alias(momanx.plugins.compose)

    alias(momanx.plugins.spotless)

    alias(libs.plugins.valkyrie)
}

android {
    namespace = "moman.icons.custommaterialsymbols"
}

spotless {
    format("svg") {
        target("src/**/*.svg")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

valkyrie {
    packageName = "moman.icons.custommaterialsymbols"
    generateAtSync = true

    imageVector {
        suppressUnusedReceiverWarning = true
    }

    iconPack {
        name = "CustomMaterialSymbols"
        targetSourceSet = "main"

        nested {
            name = "Rounded"
            sourceFolder = "rounded"
            autoMirror = false
        }
    }
}

dependencies {
    api(libs.androidx.compose.ui)
}
