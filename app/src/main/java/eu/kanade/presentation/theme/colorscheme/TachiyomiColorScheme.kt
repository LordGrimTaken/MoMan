package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * MoMan default theme: "Ink & Seal".
 *
 * Dark = warm ink-black surfaces, paper-white text.
 * Light = warm paper surfaces, ink text.
 * Accent = vermilion red, like a seal stamped on a scroll.
 *
 * Role mapping (kept compatible with the original app code):
 * secondary = unread badge, tertiary = downloaded/language badge,
 * secondaryContainer = navigation indicator.
 */
internal object TachiyomiColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFE2574F),
        onPrimary = Color(0xFF2B0806),
        primaryContainer = Color(0xFFC8302B),
        onPrimaryContainer = Color(0xFFFBE9E4),
        inversePrimary = Color(0xFFB3261E),
        secondary = Color(0xFFC8302B), // Unread badge (the seal)
        onSecondary = Color(0xFFFBE9E4), // Unread badge text
        secondaryContainer = Color(0xFF3A2B26), // Navigation indicator
        onSecondaryContainer = Color(0xFFF4EDE4),
        tertiary = Color(0xFF6FA58B), // Downloaded badge (jade)
        onTertiary = Color(0xFF0B1F17), // Downloaded badge text
        tertiaryContainer = Color(0xFF2F4A3C),
        onTertiaryContainer = Color(0xFFCDE8DA),
        background = Color(0xFF15110F),
        onBackground = Color(0xFFF4EDE4),
        surface = Color(0xFF15110F),
        onSurface = Color(0xFFF4EDE4),
        surfaceVariant = Color(0xFF211A16),
        onSurfaceVariant = Color(0xFFCFC6BB),
        surfaceTint = Color(0xFFE2574F),
        inverseSurface = Color(0xFFF4EDE4),
        inverseOnSurface = Color(0xFF15110F),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF8A7F74),
        outlineVariant = Color(0xFF3A312B),
        surfaceContainerLowest = Color(0xFF110E0C),
        surfaceContainerLow = Color(0xFF191412),
        surfaceContainer = Color(0xFF1B1613), // Navigation bar background
        surfaceContainerHigh = Color(0xFF211A16),
        surfaceContainerHighest = Color(0xFF2A221D),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFB3261E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF6D4CF),
        onPrimaryContainer = Color(0xFF3A0B08),
        inversePrimary = Color(0xFFE2574F),
        secondary = Color(0xFFC8302B), // Unread badge (the seal)
        onSecondary = Color(0xFFFFFFFF), // Unread badge text
        secondaryContainer = Color(0xFFEBD3C9), // Navigation indicator
        onSecondaryContainer = Color(0xFF3A0B08),
        tertiary = Color(0xFF3F7A5E), // Downloaded badge (jade)
        onTertiary = Color(0xFFFFFFFF), // Downloaded badge text
        tertiaryContainer = Color(0xFFCDE8DA),
        onTertiaryContainer = Color(0xFF0B2A1D),
        background = Color(0xFFF4EDE4),
        onBackground = Color(0xFF1E1814),
        surface = Color(0xFFF4EDE4),
        onSurface = Color(0xFF1E1814),
        surfaceVariant = Color(0xFFE8DED0),
        onSurfaceVariant = Color(0xFF5B4F46),
        surfaceTint = Color(0xFFB3261E),
        inverseSurface = Color(0xFF2E2620),
        inverseOnSurface = Color(0xFFF4EDE4),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF8A7F74),
        outlineVariant = Color(0xFFCFC4B6),
        surfaceContainerLowest = Color(0xFFFBF7F1),
        surfaceContainerLow = Color(0xFFF7F1E8),
        surfaceContainer = Color(0xFFEFE6DA), // Navigation bar background
        surfaceContainerHigh = Color(0xFFE8DED0),
        surfaceContainerHighest = Color(0xFFE0D5C5),
    )
}
