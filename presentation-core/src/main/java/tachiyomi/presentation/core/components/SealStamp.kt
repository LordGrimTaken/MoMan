package tachiyomi.presentation.core.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A small square "carved seal": outlined in [color], filled from the bottom up to [progress] (0..1),
 * with a thin inset border so it reads like a carved stamp.
 */
@Composable
fun SealStamp(
    progress: Float,
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary,
) {
    SealStampCanvas(
        progress = { progress },
        size = size,
        modifier = modifier,
        color = color,
        contentColor = contentColor,
    )
}

@Composable
private fun SealStampCanvas(
    progress: () -> Float,
    size: Dp,
    modifier: Modifier,
    color: Color,
    contentColor: Color,
) {
    Canvas(modifier = modifier.size(size)) {
        // Read in the draw phase so animating progress doesn't recompose
        val p = progress().coerceIn(0f, 1f)
        val outline = 1.5.dp.toPx().coerceAtLeast(1f)
        val radius = CornerRadius(2.dp.toPx())
        val half = outline / 2f
        val boxSize = Size(this.size.width - outline, this.size.height - outline)

        // Fill from the bottom up
        val fillH = this.size.height * p
        if (fillH > 0f) {
            val clipTop = this.size.height - fillH
            clipRect(top = clipTop) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(half, half),
                    size = boxSize,
                    cornerRadius = radius,
                )
            }
        }
        // Outer outline
        drawRoundRect(
            color = color,
            topLeft = Offset(half, half),
            size = boxSize,
            cornerRadius = radius,
            style = Stroke(width = outline),
        )
        // Inset carved border (contrasts where filled, subtle where empty)
        val inset = outline + 1.5.dp.toPx()
        val innerSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
        if (innerSize.width > 0f && innerSize.height > 0f) {
            val inner = Stroke(width = 0.75.dp.toPx().coerceAtLeast(0.5f))
            if (fillH > 0f) {
                clipRect(top = this.size.height - fillH) {
                    drawRoundRect(
                        color = contentColor.copy(alpha = 0.7f),
                        topLeft = Offset(inset, inset),
                        size = innerSize,
                        cornerRadius = CornerRadius(1.dp.toPx()),
                        style = inner,
                    )
                }
            }
            clipRect(bottom = this.size.height - fillH) {
                drawRoundRect(
                    color = color.copy(alpha = 0.45f),
                    topLeft = Offset(inset, inset),
                    size = innerSize,
                    cornerRadius = CornerRadius(1.dp.toPx()),
                    style = inner,
                )
            }
        }
    }
}

/** [SealStamp] whose fill animates when [progress] changes. */
@Composable
fun AnimatedSealStamp(
    progress: Float,
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary,
    durationMillis: Int = 450,
) {
    val animated = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis),
        label = "sealStampProgress",
    )
    SealStampCanvas(
        progress = { animated.value },
        size = size,
        modifier = modifier,
        color = color,
        contentColor = contentColor,
    )
}
