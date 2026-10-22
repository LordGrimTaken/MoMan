package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import mihon.icons.custommaterialsymbols.CustomMaterialSymbols
import mihon.icons.custommaterialsymbols.rounded.CropOff
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Crop
import mihon.icons.materialsymbols.rounded.Settings
import mihon.icons.materialsymbols.roundedfilled.Pause
import mihon.icons.materialsymbols.roundedfilled.PlayArrow
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun ReaderBottomBar(
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
    onPreviousChapter: () -> Unit,
    enabledPrevious: Boolean,
    onNextChapter: () -> Unit,
    enabledNext: Boolean,
    chapterButtonsReversed: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .pointerInput(Unit) {},
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (chapterButtonsReversed) {
            ChapterTextButton(
                text = stringResource(MR.strings.reader_next),
                description = stringResource(MR.strings.action_next_chapter),
                enabled = enabledNext,
                onClick = onNextChapter,
            )
        } else {
            ChapterTextButton(
                text = stringResource(MR.strings.reader_prev),
                description = stringResource(MR.strings.action_previous_chapter),
                enabled = enabledPrevious,
                onClick = onPreviousChapter,
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClickReadingMode) {
                Icon(
                    imageVector = readingMode.icon,
                    contentDescription = stringResource(MR.strings.viewer),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            IconButton(onClick = onClickOrientation) {
                Icon(
                    imageVector = orientation.icon,
                    contentDescription = stringResource(MR.strings.rotation_type),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            IconButton(onClick = onClickCropBorder) {
                Icon(
                    imageVector = if (cropEnabled) MaterialSymbols.Rounded.Crop else CustomMaterialSymbols.Rounded.CropOff,
                    contentDescription = stringResource(MR.strings.pref_crop_borders),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (autoScrollAvailable) {
                IconButton(onClick = onToggleAutoScroll) {
                    Icon(
                        imageVector = if (autoScrolling) {
                            MaterialSymbols.RoundedFilled.Pause
                        } else {
                            MaterialSymbols.RoundedFilled.PlayArrow
                        },
                        contentDescription = stringResource(
                            if (autoScrolling) MR.strings.auto_scroll_stop else MR.strings.auto_scroll_start,
                        ),
                        tint = if (autoScrolling) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }

            IconButton(onClick = onClickSettings) {
                Icon(
                    imageVector = MaterialSymbols.Rounded.Settings,
                    contentDescription = stringResource(MR.strings.action_settings),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (chapterButtonsReversed) {
            ChapterTextButton(
                text = stringResource(MR.strings.reader_prev),
                description = stringResource(MR.strings.action_previous_chapter),
                enabled = enabledPrevious,
                onClick = onPreviousChapter,
            )
        } else {
            ChapterTextButton(
                text = stringResource(MR.strings.reader_next),
                description = stringResource(MR.strings.action_next_chapter),
                enabled = enabledNext,
                onClick = onNextChapter,
            )
        }
    }
}

@Composable
private fun ChapterTextButton(
    text: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = 44.dp, minHeight = 48.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Serif),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
            maxLines = 1,
        )
    }
}
