package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.checklist_progress
import onemoretodolist.app.shared.generated.resources.checklist_progress_description
import org.jetbrains.compose.resources.stringResource

// Shared with TodoListScreen's SwipeableTodoRow, which delays the real "toggle done" dispatch by
// this long so it fires right as the strikethrough finishes wiping across the text.
internal const val STRIKETHROUGH_ANIMATION_DURATION_MS = 320

/**
 * A todo item's content - an optional single line of tag chips on top, then checkbox and text
 * centered in a row, with the formatted date (and checklist progress, if the todo has a checklist)
 * pinned to the bottom-end with a small offset. Placed inside [com.artemonre.onemoretodolist.core.designsystem.components.AppListItemCard]
 * by [TodoItemCard]; this layout stays the same across every UI style, only the surrounding card
 * container and the checkbox's own look (via [AppCheckToggle]) are allowed to differ per style.
 */
@Composable
fun TodoItemCardContent(
    text: String,
    isDone: Boolean,
    formattedDate: String,
    onToggleDone: () -> Unit,
    modifier: Modifier = Modifier,
    tags: List<TodoTag> = emptyList(),
    checklistDone: Int = 0,
    checklistTotal: Int = 0
) {
    val density = LocalDensity.current
    val textStyle = MaterialTheme.typography.bodyLarge
    val dateStyle = MaterialTheme.typography.labelSmall
    val textRowHeight = with(density) { textStyle.lineHeight.toDp() * 2 }
    val dateHeight = with(density) { dateStyle.lineHeight.toDp() }
    val dateSpacing = AppSpacing.xs

    Column(modifier = modifier.fillMaxWidth()) {
        if (tags.isNotEmpty()) {
            // One line only - tags that don't fit are simply cut off here; the detail dialog and the
            // edit form show all of them.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                tags.forEach { TodoTagChip(it) }
            }
        }
        Box(
            modifier = Modifier
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
                        modifier = Modifier
                    )
                    if (strikeProgress > 0f) {
                        Text(
                            text = text,
                            style = textStyle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier
                                .drawWithContent {
                                    clipRect(right = size.width * strikeProgress) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = AppSpacing.xs, y = AppSpacing.xs)
            ) {
                if (checklistTotal > 0) {
                    val progressDescription = stringResource(Res.string.checklist_progress_description, checklistDone, checklistTotal)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clearAndSetSemantics { contentDescription = progressDescription }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(CHECKLIST_ICON_SIZE)
                        )
                        Spacer(Modifier.width(AppSpacing.xs))
                        Text(
                            text = stringResource(Res.string.checklist_progress, checklistDone, checklistTotal),
                            style = dateStyle,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(Modifier.width(AppSpacing.s))
                }
                Text(
                    text = formattedDate,
                    style = dateStyle,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private val CHECKLIST_ICON_SIZE = 12.dp

@Preview(widthDp = 360, heightDp = 96)
@Composable
private fun TodoItemCardContentWithTagsPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoItemCardContent(
            text = "Pack for the trip",
            isDone = false,
            formattedDate = "24 Aug 2026",
            onToggleDone = {},
            tags = listOf(TodoTag("Travel", TagColor.Teal), TodoTag("Home", TagColor.Orange)),
            checklistDone = 2,
            checklistTotal = 5
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
