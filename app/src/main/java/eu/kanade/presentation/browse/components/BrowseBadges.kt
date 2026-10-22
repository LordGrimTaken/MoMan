package eu.kanade.presentation.browse.components

import androidx.compose.runtime.Composable
import moman.icons.materialsymbols.MaterialSymbols
import moman.icons.materialsymbols.rounded.CollectionsBookmark
import tachiyomi.presentation.core.components.Badge

@Composable
internal fun InLibraryBadge(enabled: Boolean) {
    if (enabled) {
        Badge(
            imageVector = MaterialSymbols.Rounded.CollectionsBookmark,
        )
    }
}
