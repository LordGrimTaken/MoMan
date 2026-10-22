package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.MangaCover as MangaCoverModel
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.util.plus

private val ShelfSidePadding = 10.dp
private val ShelfCoverGap = 4.dp
private val ShelfLedgeHeight = 4.dp
private val ShelfLedgeShadow = 1.dp
private val ShelfRaise = 6.dp
private val ShelfCoverInset = 4.dp // Matches the padding applied by the shared grid item.
private val PageEdgeWidth = 1.5.dp
private val PageEdgeColor = Color.White.copy(alpha = 0.28f)

/**
 * Cover-first shelf: rows of book covers, each row standing on a drawn ledge.
 */
@Composable
internal fun LibraryShelf(
    items: List<LibraryItem>,
    columns: Int,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = maxWidth.value
        val perRow = when {
            columns > 0 -> columns
            widthDp < 360f -> 2
            widthDp < 600f -> 3
            else -> 4
        }
        val rows = remember(items, perRow) { items.chunked(perRow) }

        FastScrollLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(vertical = 8.dp),
        ) {
            if (!searchQuery.isNullOrEmpty()) {
                item(contentType = "library_shelf_cover_search") {
                    GlobalSearchItem(
                        modifier = Modifier.fillMaxWidth(),
                        searchQuery = searchQuery,
                        onClick = onGlobalSearchClicked,
                    )
                }
            }

            items(
                items = rows,
                key = { row -> row.first().id },
                contentType = { "library_shelf_cover_row" },
            ) { row ->
                ShelfCoverRow(
                    row = row,
                    perRow = perRow,
                    selection = selection,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            }
        }
    }
}

@Composable
private fun ShelfCoverRow(
    row: List<LibraryItem>,
    perRow: Int,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val ledgeColor = remember(onSurface) { onSurface.copy(alpha = 0.18f) }
    val shadowColor = remember(onSurface) { onSurface.copy(alpha = 0.07f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val ledge = ShelfLedgeHeight.toPx()
                val shadow = ShelfLedgeShadow.toPx()
                drawRect(
                    color = ledgeColor,
                    topLeft = Offset(0f, size.height - ledge - shadow),
                    size = Size(size.width, ledge),
                )
                drawRect(
                    color = shadowColor,
                    topLeft = Offset(0f, size.height - shadow),
                    size = Size(size.width, shadow),
                )
            }
            .padding(
                start = ShelfSidePadding,
                end = ShelfSidePadding,
                top = 10.dp,
                bottom = ShelfLedgeHeight + ShelfLedgeShadow + 6.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(ShelfCoverGap),
    ) {
        row.forEach { libraryItem ->
            Box(modifier = Modifier.weight(1f)) {
                ShelfCover(
                    libraryItem = libraryItem,
                    isSelected = libraryItem.libraryManga.manga.id in selection,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            }
        }
        // Keep the last row's covers the same width as full rows.
        repeat(perRow - row.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ShelfCover(
    libraryItem: LibraryItem,
    isSelected: Boolean,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
) {
    val manga = libraryItem.libraryManga.manga
    val primary = MaterialTheme.colorScheme.primary
    val ratio = MangaCover.Book.ratio

    Box(
        modifier = Modifier
            .graphicsLayer { translationY = if (isSelected) -ShelfRaise.toPx() else 0f }
            .drawWithContent {
                drawContent()
                val inset = ShelfCoverInset.toPx()
                val coverWidth = size.width - inset * 2
                val coverHeight = coverWidth / ratio
                val edge = PageEdgeWidth.toPx()
                // Subtle lighter "page edge" along the cover's right side.
                drawRect(
                    color = PageEdgeColor,
                    topLeft = Offset(inset + coverWidth - edge, inset),
                    size = Size(edge, coverHeight),
                )
                if (isSelected) {
                    val stroke = 2.dp.toPx()
                    drawRect(
                        color = primary,
                        topLeft = Offset(inset + stroke / 2, inset + stroke / 2),
                        size = Size(coverWidth - stroke, coverHeight - stroke),
                        style = Stroke(width = stroke),
                    )
                }
            },
    ) {
        MangaComfortableGridItem(
            readProgress = libraryItem.libraryManga.readFraction(),
            isSelected = isSelected,
            title = manga.title,
            titleFontFamily = FontFamily.Serif,
            coverData = MangaCoverModel(
                mangaId = manga.id,
                sourceId = manga.source,
                isMangaFavorite = manga.favorite,
                url = manga.thumbnailUrl,
                lastModified = manga.coverLastModified,
            ),
            coverBadgeStart = {
                DownloadsBadge(count = libraryItem.badges.downloadCount)
                UnreadBadge(count = libraryItem.badges.unreadCount)
            },
            coverBadgeEnd = {
                LanguageBadge(
                    isLocal = libraryItem.badges.isLocal,
                    sourceLanguage = libraryItem.badges.sourceLanguage,
                )
            },
            onLongClick = { onLongClick(libraryItem.libraryManga) },
            onClick = { onClick(libraryItem.libraryManga) },
        )
    }
}
