package eu.kanade.presentation.reader.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val ScrubberWidth = 40.dp
private val TrackWidth = 3.dp
private val ThumbSize = 10.dp
private val BubbleHeight = 28.dp
private val BubbleGap = 26.dp

/**
 * Thin vertical "ink" scrubber for seeking through the pages of the current chapter.
 *
 * Pages are 1-based for display ([currentPage], [totalPages]); [onPageIndexChange] receives a 0-based index.
 * Nothing is drawn when there is only a single page.
 *
 * @param onLeft whether the scrubber sits on the physical left edge (bubble opens to the right).
 */
@Composable
fun EdgeScrubber(
    currentPage: Int,
    totalPages: Int,
    onPageIndexChange: (Int) -> Unit,
    onPageIndexChangeFinished: () -> Unit,
    onLeft: Boolean,
    modifier: Modifier = Modifier,
) {
    if (totalPages <= 1) return

    val haptic = LocalHapticFeedback.current
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val fillColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    val scrimColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.55f)
    val bubbleColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val bubbleTextColor = MaterialTheme.colorScheme.onSurface
    val bubbleStyle = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif)

    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var dragIndex by remember { mutableStateOf(-1) }

    val latestOnChange by rememberUpdatedState(onPageIndexChange)
    val latestOnFinished by rememberUpdatedState(onPageIndexChangeFinished)
    val latestTotal by rememberUpdatedState(totalPages)

    val currentFraction = ((currentPage - 1).toFloat() / (totalPages - 1)).coerceIn(0f, 1f)
    val fraction = if (dragging) dragFraction else currentFraction
    val shownPage = if (dragging && dragIndex >= 0) dragIndex + 1 else currentPage

    // Keep the scrubber's geometry independent of the app's layout direction
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .width(ScrubberWidth)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val thumbPx = ThumbSize.toPx()
                        val range = (size.height - thumbPx).coerceAtLeast(1f)

                        fun seek(y: Float) {
                            val f = ((y - thumbPx / 2f) / range).coerceIn(0f, 1f)
                            val index = (f * (latestTotal - 1)).roundToInt().coerceIn(0, latestTotal - 1)
                            dragFraction = f
                            if (index != dragIndex) {
                                dragIndex = index
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                latestOnChange(index)
                            }
                        }

                        dragIndex = -1
                        dragging = true
                        seek(down.position.y)
                        down.consume()
                        drag(down.id) { change ->
                            seek(change.position.y)
                            change.consume()
                        }
                        dragging = false
                        dragIndex = -1
                        latestOnFinished()
                    }
                },
        ) {
            val thumbTop = (maxHeight - ThumbSize) * fraction

            Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                val thumbPx = ThumbSize.toPx()
                val trackPx = TrackWidth.toPx()
                val centerX = size.width / 2f
                val trackTop = thumbPx / 2f
                val trackBottom = size.height - thumbPx / 2f
                val thumbY = (size.height - thumbPx) * fraction

                // Soft backing so the track stays legible over any page art
                val scrimW = 16.dp.toPx()
                drawRoundRect(
                    color = scrimColor,
                    topLeft = Offset(centerX - scrimW / 2f, trackTop - 6.dp.toPx()),
                    size = Size(scrimW, (trackBottom - trackTop) + 12.dp.toPx()),
                    cornerRadius = CornerRadius(scrimW / 2f),
                )
                // Ink track
                drawRect(
                    color = trackColor,
                    topLeft = Offset(centerX - trackPx / 2f, trackTop),
                    size = Size(trackPx, trackBottom - trackTop),
                )
                // Vermilion filled portion
                drawRect(
                    color = fillColor,
                    topLeft = Offset(centerX - trackPx / 2f, trackTop),
                    size = Size(trackPx, (thumbY + thumbPx / 2f - trackTop).coerceAtLeast(0f)),
                )
                // Chapter boundary dots (start and end of the current chapter)
                drawCircle(dotColor, radius = 2.5.dp.toPx(), center = Offset(centerX, trackTop))
                drawCircle(dotColor, radius = 2.5.dp.toPx(), center = Offset(centerX, trackBottom))
                // Seal thumb
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(centerX - thumbPx / 2f, thumbY),
                    size = Size(thumbPx, thumbPx),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }

            if (dragging) {
                Box(
                    modifier = Modifier
                        .offset(y = thumbTop + ThumbSize / 2 - BubbleHeight / 2)
                        .fillMaxWidth()
                        .wrapContentWidth(
                            align = if (onLeft) Alignment.Start else Alignment.End,
                            unbounded = true,
                        )
                        .padding(start = if (onLeft) BubbleGap else 0.dp, end = if (onLeft) 0.dp else BubbleGap)
                        .height(BubbleHeight)
                        .background(bubbleColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$shownPage / $totalPages",
                        style = bubbleStyle,
                        color = bubbleTextColor,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}
