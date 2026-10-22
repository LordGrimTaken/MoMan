package eu.kanade.presentation.reader.appbars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.reader.components.ChapterNavigatorType
import eu.kanade.presentation.reader.components.EdgeScrubber
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import tachiyomi.presentation.core.components.material.padding

private const val ReaderBarsDurationMillis = 220
private val ReaderBarsDrift = 12.dp
private val readerBarsSlideAnimationSpec = tween<IntOffset>(ReaderBarsDurationMillis, easing = FastOutSlowInEasing)
private val readerBarsFadeAnimationSpec = tween<Float>(ReaderBarsDurationMillis, easing = FastOutSlowInEasing)

@Composable
fun ReaderAppBars(
    visible: Boolean,

    mangaTitle: String?,
    chapterTitle: String?,
    navigateUp: () -> Unit,
    onClickTopAppBar: () -> Unit,
    bookmarked: Boolean,
    onToggleBookmarked: () -> Unit,
    onOpenInWebView: (() -> Unit)?,
    onOpenInBrowser: (() -> Unit)?,
    onShare: (() -> Unit)?,

    chapterNavigatorType: ChapterNavigatorType,
    verticalNavigatorHeight: Float,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    currentPage: Int,
    totalPages: Int,
    onPageIndexChange: (Int) -> Unit,
    onPageIndexChangeFinished: () -> Unit,

    readingMode: ReadingMode,
    onClickReadingMode: () -> Unit,
    orientation: ReaderOrientation,
    onClickOrientation: () -> Unit,
    cropEnabled: Boolean,
    onClickCropBorder: () -> Unit,
    onClickSettings: () -> Unit,

    autoScrollAvailable: Boolean,
    autoScrolling: Boolean,
    onToggleAutoScroll: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f)
    val hairlineColor = MaterialTheme.colorScheme.outlineVariant

    val driftPx = with(LocalDensity.current) { ReaderBarsDrift.roundToPx() }

    Column(modifier = Modifier.fillMaxHeight()) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(readerBarsSlideAnimationSpec) { -driftPx } + fadeIn(readerBarsFadeAnimationSpec),
            exit = slideOutVertically(readerBarsSlideAnimationSpec) { -driftPx } + fadeOut(readerBarsFadeAnimationSpec),
        ) {
            ReaderTopBar(
                modifier = Modifier
                    .background(backgroundColor)
                    .drawBehind {
                        val stroke = 0.5.dp.toPx()
                        drawRect(
                            color = hairlineColor,
                            topLeft = Offset(0f, size.height - stroke),
                            size = Size(size.width, stroke),
                        )
                    }
                    .clickable(onClick = onClickTopAppBar),
                mangaTitle = mangaTitle,
                chapterTitle = chapterTitle,
                navigateUp = navigateUp,
                bookmarked = bookmarked,
                onToggleBookmarked = onToggleBookmarked,
                onOpenInWebView = onOpenInWebView,
                onOpenInBrowser = onOpenInBrowser,
                onShare = onShare,
            )
        }

        // Edge scrubber between the bars; trailing edge for LTR/vertical modes, leading edge for RTL
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val scrubberAlphaState = animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = readerBarsFadeAnimationSpec,
                label = "scrubberAlpha",
            )
            // Only recompose when the scrubber appears/disappears, not on every animation frame
            val scrubberShown by remember { derivedStateOf { scrubberAlphaState.value > 0f } }
            if (scrubberShown) {
                Box(
                    modifier = Modifier
                        .align(if (chapterNavigatorType.scrubberOnLeft()) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight(if (chapterNavigatorType.isHorizontal()) 1f else verticalNavigatorHeight)
                        .padding(vertical = MaterialTheme.padding.small)
                        .graphicsLayer { alpha = scrubberAlphaState.value },
                ) {
                    EdgeScrubber(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageIndexChange = onPageIndexChange,
                        onPageIndexChangeFinished = onPageIndexChangeFinished,
                        onLeft = chapterNavigatorType.scrubberOnLeft(),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(readerBarsSlideAnimationSpec) { driftPx } + fadeIn(readerBarsFadeAnimationSpec),
            exit = slideOutVertically(readerBarsSlideAnimationSpec) { driftPx } + fadeOut(readerBarsFadeAnimationSpec),
        ) {
            ReaderBottomBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .drawBehind {
                        drawRect(
                            color = hairlineColor,
                            size = Size(size.width, 0.5.dp.toPx()),
                        )
                    }
                    .padding(horizontal = MaterialTheme.padding.small)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                readingMode = readingMode,
                onClickReadingMode = onClickReadingMode,
                orientation = orientation,
                onClickOrientation = onClickOrientation,
                cropEnabled = cropEnabled,
                onClickCropBorder = onClickCropBorder,
                onClickSettings = onClickSettings,
                autoScrollAvailable = autoScrollAvailable,
                autoScrolling = autoScrolling,
                onToggleAutoScroll = onToggleAutoScroll,
                onPreviousChapter = onPreviousChapter,
                enabledPrevious = enabledPrevious,
                onNextChapter = onNextChapter,
                enabledNext = enabledNext,
                chapterButtonsReversed = chapterNavigatorType == ChapterNavigatorType.HORIZONTAL_RTL,
            )
        }
    }
}
