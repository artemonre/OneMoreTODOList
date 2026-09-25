package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppListItemCard
import com.artemonre.onemoretodolist.core.designsystem.components.cloudTexture
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TopTodoAttention

/**
 * A single todo item, built from the domain-agnostic [AppListItemCard] plus todo-specific
 * [TodoItemCardContent]. Feature-specific composition lives here, not in `core.designsystem`.
 *
 * [id] seeds the card's cloudTexture decoration (see CloudTexture.kt) - trying this out per user
 * request, not a finalized design. Same id always draws the same texture; different todos get
 * different-looking ones.
 */
@Composable
fun TodoItemCard(
    id: String,
    text: String,
    isDone: Boolean,
    formattedDate: String,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    attention: TopTodoAttention = TopTodoAttention.None
) {
    AppListItemCard(
        onClick = onClick,
        modifier = modifier,
        containerColor = attention.containerColor()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .cloudTexture(seed = id.hashCode(), color = MaterialTheme.colorScheme.outline)
        ) {
            TodoItemCardContent(
                text = text,
                isDone = isDone,
                formattedDate = formattedDate,
                onToggleDone = onToggleDone
            )
        }
    }
}

@Composable
internal fun TopTodoAttention.containerColor(): Color? = when (this) {
    TopTodoAttention.None -> null
    TopTodoAttention.Primary -> MaterialTheme.colorScheme.primary
    TopTodoAttention.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
    TopTodoAttention.Error -> MaterialTheme.colorScheme.error
}

@Preview(widthDp = 360, heightDp = 100)
@Composable
private fun TodoItemCardPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoItemCard(
            id = "preview-1",
            text = "Buy groceries",
            isDone = false,
            formattedDate = "24 Aug 2026",
            onToggleDone = {},
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}

@Preview(widthDp = 360, heightDp = 100)
@Composable
private fun TodoItemCardAttentionPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoItemCard(
            id = "preview-2",
            text = "Water the plants",
            isDone = false,
            formattedDate = "24 Aug 2026",
            onToggleDone = {},
            onClick = {},
            attention = TopTodoAttention.Error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
