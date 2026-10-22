package tachiyomi.presentation.core.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.icerock.moko.resources.StringResource
import tachiyomi.presentation.core.components.ActionButton
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.secondaryItemAlpha

data class EmptyScreenAction(
    val stringRes: StringResource,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun EmptyScreen(
    stringRes: StringResource,
    modifier: Modifier = Modifier,
    actions: List<EmptyScreenAction>? = null,
) {
    EmptyScreen(
        message = stringResource(stringRes),
        modifier = modifier,
        actions = actions,
    )
}

@Composable
fun EmptyScreen(
    message: String,
    modifier: Modifier = Modifier,
    actions: List<EmptyScreenAction>? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        InkEnso(modifier = Modifier.size(132.dp))

        Text(
            text = message,
            modifier = Modifier
                .padding(top = 24.dp)
                .secondaryItemAlpha(),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
            textAlign = TextAlign.Center,
        )

        if (!actions.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            ) {
                actions.fastForEach {
                    ActionButton(
                        modifier = Modifier.weight(1f),
                        title = stringResource(it.stringRes),
                        icon = it.icon,
                        onClick = it.onClick,
                    )
                }
            }
        }
    }
}

private const val ENSO_START_ANGLE = -60f
private const val ENSO_SWEEP = 300f

/** Hand-drawn style ink circle (enso) that draws itself in, with a vermilion seal at the gap. */
@Composable
private fun InkEnso(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val progress = remember { Animatable(if (reduceMotion) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reduceMotion) {
            progress.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
        }
    }
    val inkColor = MaterialTheme.colorScheme.onSurfaceVariant
    val sealColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier) {
        val p = progress.value
        val pad = 14.dp.toPx()
        val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
        val topLeft = Offset(pad, pad)
        // Three overlapping strokes of varying weight approximate a brush stroke.
        drawArc(
            color = inkColor.copy(alpha = 0.22f),
            startAngle = ENSO_START_ANGLE,
            sweepAngle = ENSO_SWEEP * p,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = inkColor.copy(alpha = 0.32f),
            startAngle = ENSO_START_ANGLE + 6f,
            sweepAngle = (ENSO_SWEEP - 40f) * p,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
        )
        drawArc(
            color = inkColor.copy(alpha = 0.42f),
            startAngle = ENSO_START_ANGLE + 2f,
            sweepAngle = (ENSO_SWEEP - 90f) * p,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )

        // Seal at the gap (midpoint of the open part of the circle)
        val gapAngle = Math.toRadians((ENSO_START_ANGLE - (360f - ENSO_SWEEP) / 2f).toDouble())
        val r = arcSize.width / 2f
        val cx = size.width / 2f + (r * cos(gapAngle)).toFloat()
        val cy = size.height / 2f + (r * sin(gapAngle)).toFloat()
        val sealSize = 11.dp.toPx()
        drawRoundRect(
            color = sealColor.copy(alpha = p),
            topLeft = Offset(cx - sealSize / 2f, cy - sealSize / 2f),
            size = Size(sealSize, sealSize),
            cornerRadius = CornerRadius(2.dp.toPx()),
        )
    }
}
