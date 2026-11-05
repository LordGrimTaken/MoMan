plugins {
    alias(mihonx.plugins.android.library)
    alias(mihonx.plugins.compose)

    alias(mihonx.plugins.spotless)

    alias(libs.plugins.valkyrie)
}

android {
    namespace = "com.moman.icons.custommaterialsymbols"
}

spotless {
    format("svg") {
        target("src/**/*.svg")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

valkyrie {
    packageName = "com.moman.icons.custommaterialsymbols"
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
