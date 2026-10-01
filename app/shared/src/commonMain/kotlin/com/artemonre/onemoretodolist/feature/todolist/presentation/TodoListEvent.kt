package com.artemonre.onemoretodolist.feature.todolist.presentation

import org.jetbrains.compose.resources.StringResource

sealed interface TodoListEvent {
    data object ShowAddTodoSheet : TodoListEvent
    data object ShowAddTodoFullScreenDialog : TodoListEvent
    data class ShowEditTodoSheet(val item: TodoItemUi) : TodoListEvent
    data class ShowUndoSnackbar(val message: StringResource) : TodoListEvent
    data object ShowPhotoCapture : TodoListEvent
    data class ShowScannedTextReview(val lines: List<String>) : TodoListEvent
    data class ShowSnackbar(val message: StringResource) : TodoListEvent
}
