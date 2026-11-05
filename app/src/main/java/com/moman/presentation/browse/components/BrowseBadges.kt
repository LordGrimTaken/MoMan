package com.moman.presentation.browse.components

import androidx.compose.runtime.Composable
import com.moman.icons.materialsymbols.MaterialSymbols
import com.moman.icons.materialsymbols.rounded.CollectionsBookmark
import com.moman.presentation.core.components.Badge

@Composable
internal fun InLibraryBadge(enabled: Boolean) {
    if (enabled) {
        Badge(
            imageVector = MaterialSymbols.Rounded.CollectionsBookmark,
        )
    }
}
