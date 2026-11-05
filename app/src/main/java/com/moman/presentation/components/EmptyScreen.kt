package com.moman.presentation.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.moman.presentation.theme.TachiyomiPreviewTheme
import com.moman.icons.materialsymbols.MaterialSymbols
import com.moman.icons.materialsymbols.automirroredrounded.Help
import com.moman.icons.materialsymbols.rounded.Refresh
import com.moman.i18n.MR
import com.moman.presentation.core.screens.EmptyScreen
import com.moman.presentation.core.screens.EmptyScreenAction

@PreviewLightDark
@Composable
private fun NoActionPreview() {
    TachiyomiPreviewTheme {
        Surface {
            EmptyScreen(
                stringRes = MR.strings.empty_screen,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun WithActionPreview() {
    TachiyomiPreviewTheme {
        Surface {
            EmptyScreen(
                stringRes = MR.strings.empty_screen,
                actions = listOf(
                    EmptyScreenAction(
                        stringRes = MR.strings.action_retry,
                        icon = MaterialSymbols.Rounded.Refresh,
                        onClick = {},
                    ),
                    EmptyScreenAction(
                        stringRes = MR.strings.getting_started_guide,
                        icon = MaterialSymbols.AutoMirroredRounded.Help,
                        onClick = {},
                    ),
                ),
            )
        }
    }
}
