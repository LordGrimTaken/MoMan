package eu.kanade.presentation.library.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.tachiyomi.ui.library.LibraryItem
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.presentation.core.components.FastScrollLazyColumn
import tachiyomi.presentation.core.util.plus
import kotlin.math.min

private val SpineGap = 5.dp
private val ShelfHorizontalPadding = 12.dp
private val SpineRiseOnSelect = 10.dp
private val SpineTextColor = Color(0xFFF1E6D3)

/** Curated, ink-friendly spine colours (dark enough for light serif text on both themes). */
private val SpinePalette = listOf(
    Color(0xFF8E2420), // deep vermilion
    Color(0xFF2F6B55), // jade
    Color(0xFF2B3A67), // indigo
    Color(0xFF8A5A14), // ochre
    Color(0xFF5E2C57), // plum
    Color(0xFF3F4A56), // slate
)

private class SpineSpec(
    val item: LibraryItem,
    val width: Dp,
    val height: Dp,
    val color: Color,
)

private fun spineSpecFor(item: LibraryItem): SpineSpec {
    val manga = item.libraryManga.manga
    val hash = manga.title.hashCode()
    val mixed = (hash xor (hash ushr 16)) * 0x45d9f3b
    val positive = mixed and Int.MAX_VALUE
    val chapters = min(item.libraryManga.totalChapters, 200L).toFloat()
    val width = 28f + (chapters / 200f) * 16f
    val height = 150f + (positive % 41)
    return SpineSpec(
        item = item,
        width = width.dp,
        height = height.dp,
        color = SpinePalette[(positive / 41) % SpinePalette.size],
    )
}

@Composable
internal fun LibraryShelf(
    items: List<LibraryItem>,
    contentPadding: PaddingValues,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
    searchQuery: String?,
    onGlobalSearchClicked: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val availableWidth = maxWidth - ShelfHorizontalPadding * 2
        val rows = remember(items, availableWidth) {
            val specs = items.map(::spineSpecFor)
            val result = ArrayList<List<SpineSpec>>()
            var current = ArrayList<SpineSpec>()
            var used = 0.dp
            for (spec in specs) {
                val needed = if (current.isEmpty()) spec.width else spec.width + SpineGap
                if (current.isNotEmpty() && used + needed > availableWidth) {
                    result.add(current)
                    current = ArrayList()
                    used = 0.dp
                }
                used += if (current.isEmpty()) spec.width else spec.width + SpineGap
                current.add(spec)
            }
            if (current.isNotEmpty()) result.add(current)
            result
        }

        FastScrollLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding + PaddingValues(vertical = 8.dp),
        ) {
            if (!searchQuery.isNullOrEmpty()) {
                item(contentType = "library_shelf_search") {
                    GlobalSearchItem(
                        modifier = Modifier.fillMaxWidth(),
                        searchQuery = searchQuery,
                        onClick = onGlobalSearchClicked,
                    )
                }
            }

            items(
                items = rows,
                key = { row -> row.first().item.id },
                contentType = { "library_shelf_row" },
            ) { row ->
                ShelfRow(
                    row = row,
                    selection = selection,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
            }
        }
    }
}

@Composable
private fun ShelfRow(
    row: List<SpineSpec>,
    selection: Set<Long>,
    onClick: (LibraryManga) -> Unit,
    onLongClick: (LibraryManga) -> Unit,
) {
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShelfHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(SpineGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            row.forEach { spec ->
                val manga = spec.item.libraryManga
                Spine(
                    spec = spec,
                    isSelected = manga.id in selection,
                    onClick = { onClick(manga) },
                    onLongClick = { onLongClick(manga) },
                )
            }
        }
        // Shelf board with a subtle shadow line beneath.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .offset(y = 4.dp)
                .drawBehind {
                    drawRect(color = lineColor, size = androidx.compose.ui.geometry.Size(size.width, 3.dp.toPx()))
                    drawRect(
                        color = lineColor.copy(alpha = 0.08f),
                        topLeft = Offset(0f, 3.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(size.width, 1.dp.toPx()),
                    )
                },
        )
    }
}

@Composable
private fun Spine(
    spec: SpineSpec,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val rise by animateDpAsState(
        targetValue = if (isSelected) SpineRiseOnSelect else 0.dp,
        label = "shelf_spine_rise",
    )
    val seal = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 1.dp, bottomEnd = 1.dp)
    val unread = spec.item.unreadCount
    val title = spec.item.libraryManga.manga.title

    Box(
        modifier = Modifier
            .offset { IntOffset(0, -rise.roundToPx()) }
            .size(width = spec.width, height = spec.height)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        spec.color.copy(alpha = 0.85f),
                        spec.color,
                        spec.color.copy(alpha = 0.9f),
                    ),
                ),
            )
            .drawBehind {
                val band = Color.White.copy(alpha = 0.22f)
                val stroke = 1.dp.toPx()
                listOf(26.dp.toPx(), 30.dp.toPx(), size.height - 14.dp.toPx(), size.height - 10.dp.toPx())
                    .forEach { y ->
                        drawLine(band, Offset(0f, y), Offset(size.width, y), strokeWidth = stroke)
                    }
            }
            .then(
                if (isSelected) {
                    Modifier.border(BorderStroke(2.dp, seal), shape)
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        if (unread > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 5.dp)
                    .size(14.dp)
                    .background(seal),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unread > 99) "99" else unread.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 36.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                color = SpineTextColor,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .layout { measurable, constraints ->
                        // Swap axes so the text lays out along the spine height, then rotate.
                        val placeable = measurable.measure(
                            Constraints(
                                minWidth = 0,
                                maxWidth = constraints.maxHeight,
                                minHeight = 0,
                                maxHeight = constraints.maxWidth,
                            ),
                        )
                        layout(placeable.height, placeable.width) {
                            placeable.place(
                                x = -(placeable.width / 2 - placeable.height / 2),
                                y = -(placeable.height / 2 - placeable.width / 2),
                            )
                        }
                    }
                    .rotate(90f),
            )
        }
    }
}
