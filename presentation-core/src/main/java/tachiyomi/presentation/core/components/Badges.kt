package tachiyomi.presentation.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val StampShape = RoundedCornerShape(2.dp)

@Composable
fun BadgeGroup(
    modifier: Modifier = Modifier,
    shape: Shape = StampShape,
    content: @Composable RowScope.() -> Unit,
) {
    Row(modifier = modifier.clip(shape)) {
        content()
    }
}

@Composable
fun Badge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    textColor: Color = MaterialTheme.colorScheme.onSecondary,
    shape: Shape = StampShape,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun Badge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    iconColor: Color = MaterialTheme.colorScheme.onSecondary,
    shape: Shape = StampShape,
) {
    val iconContentPlaceholder = "[icon]"
    val text = buildAnnotatedString {
        appendInlineContent(iconContentPlaceholder)
    }
    val inlineContent = mapOf(
        Pair(
            iconContentPlaceholder,
            InlineTextContent(
                Placeholder(
                    width = MaterialTheme.typography.labelSmall.fontSize,
                    height = MaterialTheme.typography.labelSmall.fontSize,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                ),
            ) {
                Icon(
                    imageVector = imageVector,
                    tint = iconColor,
                    contentDescription = null,
                )
            },
        ),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .padding(horizontal = 4.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            inlineContent = inlineContent,
            color = iconColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/**
 * Seal-style badge: a vermilion square stamp with an inset carved border and the [text] inside.
 */
@Composable
fun SealBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.secondary,
    textColor: Color = MaterialTheme.colorScheme.onSecondary,
) {
    Box(
        modifier = modifier
            .clip(StampShape)
            .background(color)
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .padding(1.5.dp)
            .border(0.75.dp, textColor.copy(alpha = 0.6f), RoundedCornerShape(1.dp))
            .padding(horizontal = 3.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
