package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// Shared with TodoListScreen's SwipeableTodoRow, which delays the real "toggle done" dispatch by
// this long so it fires right as the strikethrough finishes wiping across the text.
internal const val STRIKETHROUGH_ANIMATION_DURATION_MS = 320

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
            Box(modifier = Modifier.weight(1f)) {
                val strikeProgress by animateFloatAsState(
                    targetValue = if (isDone) 1f else 0f,
                    animationSpec = tween(durationMillis = STRIKETHROUGH_ANIMATION_DURATION_MS),
                    label = "todoStrikethrough"
                )
                Text(
                    text = text,
                    style = textStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 4.dp)
                )
                if (strikeProgress > 0f) {
                    Text(
                        text = text,
                        style = textStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .drawWithContent {
                                clipRect(right = size.width * strikeProgress) {
                                    this@drawWithContent.drawContent()
                                }
                            }
                    )
                }
            }
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

@Preview(widthDp = 360, heightDp = 68)
@Composable
private fun TodoItemCardContentPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoItemCardContent(
            text = "Buy groceries",
            isDone = false,
            formattedDate = "24 Aug 2026",
            onToggleDone = {}
        )
    }
}
