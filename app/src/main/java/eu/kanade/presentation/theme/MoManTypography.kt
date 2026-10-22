package eu.kanade.presentation.theme

import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * MoMan type scale: a calligraphic serif for display, headline and large titles
 * (screen titles, section names), clean sans for everything else.
 */
internal val MoManTypography: Typography = Typography().let { base ->
    val serif = FontFamily.Serif
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = serif),
        displayMedium = base.displayMedium.copy(fontFamily = serif),
        displaySmall = base.displaySmall.copy(fontFamily = serif),
        headlineLarge = base.headlineLarge.copy(fontFamily = serif, letterSpacing = 0.5.sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = serif, letterSpacing = 0.5.sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = serif, letterSpacing = 0.5.sp),
        titleLarge = base.titleLarge.copy(
            fontFamily = serif,
            fontWeight = FontWeight.Normal,
            letterSpacing = 1.sp,
        ),
    )
}

/**
 * MoMan shapes: squarer than stock Material, like seal stamps and scroll edges.
 */
internal val MoManShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp),
)
