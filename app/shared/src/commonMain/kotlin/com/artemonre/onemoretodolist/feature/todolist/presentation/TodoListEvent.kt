package com.artemonre.onemoretodolist.feature.todolist.presentation

sealed interface TodoListEvent {
    data class ShowUndoSnackbar(val message: String) : TodoListEvent
    data class ShowSnackbar(val message: String) : TodoListEvent
}
