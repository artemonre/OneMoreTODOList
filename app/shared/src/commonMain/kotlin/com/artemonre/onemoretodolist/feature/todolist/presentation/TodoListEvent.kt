package com.artemonre.onemoretodolist.feature.todolist.presentation

import org.jetbrains.compose.resources.StringResource

sealed interface TodoListEvent {
    // draft is the unsaved add draft to reopen with, null for a fresh form.
    data class ShowAddTodoSheet(val draft: TodoDraft?) : TodoListEvent
    data class ShowAddTodoFullScreenDialog(val draft: TodoDraft?) : TodoListEvent
    data class ShowEditTodoSheet(val item: TodoItemUi) : TodoListEvent
    data class ShowUndoSnackbar(val message: StringResource) : TodoListEvent
    data object ShowPhotoCapture : TodoListEvent
    data class ShowScannedTextReview(val paragraphs: List<ScannedParagraph>) : TodoListEvent
    data class ShowSnackbar(val message: StringResource) : TodoListEvent
}
