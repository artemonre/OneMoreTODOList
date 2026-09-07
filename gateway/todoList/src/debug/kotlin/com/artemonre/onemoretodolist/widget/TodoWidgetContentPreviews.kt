@file:OptIn(ExperimentalGlancePreviewApi::class)

package com.artemonre.onemoretodolist.widget

import androidx.compose.runtime.Composable
import androidx.glance.color.ColorProviders
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import com.artemonre.onemoretodolist.core.designsystem.theme.toColorPalette
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoItemUi

private fun previewTodoItem(id: String, text: String) = TodoItemUi(
    id = id,
    text = text,
    status = TodoStatus.Active,
    sortOrder = 0,
    formattedDate = "04 Sep 2026"
)

private val previewPalette = ColorPaletteOption.Default.toColorPalette()
private val previewColors: ColorProviders =
    androidx.glance.material3.ColorProviders(light = previewPalette.light, dark = previewPalette.dark)
private val previewBackground =
    androidx.glance.color.ColorProvider(day = previewPalette.light.surfaceContainer, night = previewPalette.dark.surfaceContainer)

// Matches SIZE_2X2 in TodoWidget.kt.
@Preview(widthDp = 110, heightDp = 110)
@Composable
private fun TodoWidgetContentPreview() {
    TodoWidgetContent(
        todos = listOf(
            previewTodoItem(id = "1", text = "Buy groceries"),
            previewTodoItem(id = "2", text = "Write project report")
        ),
        colors = previewColors,
        background = previewBackground
    )
}

// Matches SIZE_1X1 in TodoWidget.kt.
@Preview(widthDp = 40, heightDp = 40)
@Composable
private fun TodoWidgetContentCompactPreview() {
    TodoWidgetContentCompact(activeCount = 3, colors = previewColors, background = previewBackground)
}

// Matches SIZE_2X1 in TodoWidget.kt.
@Preview(widthDp = 110, heightDp = 40)
@Composable
private fun TodoWidgetContentRowPreview() {
    TodoWidgetContentRow(
        topTodo = previewTodoItem(id = "1", text = "Buy groceries"),
        colors = previewColors,
        background = previewBackground
    )
}

@Preview(widthDp = 110, heightDp = 40)
@Composable
private fun TodoWidgetContentRowEmptyPreview() {
    TodoWidgetContentRow(topTodo = null, colors = previewColors, background = previewBackground)
}

@Preview(widthDp = 110, heightDp = 40)
@Composable
private fun TodoWidgetRowPreview() {
    TodoWidgetRow(item = previewTodoItem(id = "1", text = "Buy groceries"))
}
