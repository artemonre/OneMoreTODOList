package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle

/**
 * A todo item's content - checkbox and text centered in a row, formatted date pinned to the
 * bottom-end with a small offset. Placed inside [com.artemonre.onemoretodolist.core.designsystem.components.AppListItemCard]
 * by [TodoItemCard]; this layout stays the same across every UI style, only the surrounding card
 * container and the checkbox's own look (via [AppCheckToggle]) are allowed to differ per style.
 */
@Composable
fun TodoItemCardContent(
    text: String,
    isDone: Boolean,
    formattedDate: String,
    onToggleDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textStyle = MaterialTheme.typography.bodyLarge
    val dateStyle = MaterialTheme.typography.labelSmall
    val textRowHeight = with(density) { textStyle.lineHeight.toDp() * 2 }
    val dateHeight = with(density) { dateStyle.lineHeight.toDp() }
    val dateSpacing = 4.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .height(textRowHeight + dateSpacing + dateHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckToggle(
                checked = isDone,
                onCheckedChange = { onToggleDone() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = textStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = formattedDate,
            style = dateStyle,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
        )
    }
}
