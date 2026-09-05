package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppListItemCard
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

/**
 * A single todo item, built from the domain-agnostic [AppListItemCard] plus todo-specific
 * [TodoItemCardContent]. Feature-specific composition lives here, not in `core.designsystem`.
 */
@Composable
fun TodoItemCard(
    text: String,
    isDone: Boolean,
    formattedDate: String,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppListItemCard(
        onClick = onClick,
        modifier = modifier
    ) {
        TodoItemCardContent(
            text = text,
            isDone = isDone,
            formattedDate = formattedDate,
            onToggleDone = onToggleDone
        )
    }
}

@Preview(widthDp = 360, heightDp = 100)
@Composable
private fun TodoItemCardPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoItemCard(
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
