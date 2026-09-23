package com.artemonre.onemoretodolist.feature.todolist.presentation

sealed interface TodoListEvent {
    data object ShowAddTodoSheet : TodoListEvent
    data object ShowAddTodoFullScreenDialog : TodoListEvent
    data class ShowEditTodoSheet(val item: TodoItemUi) : TodoListEvent
    data class ShowUndoSnackbar(val message: String) : TodoListEvent
    // A todo was just created or completed - the interstitial ad trigger listens for this (see
    // TodoListRoot); at most one interstitial ever actually shows per day regardless of how many
    // of these fire.
    data object TodoActivityHappened : TodoListEvent
}
