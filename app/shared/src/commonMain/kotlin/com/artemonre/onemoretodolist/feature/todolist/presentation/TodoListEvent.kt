package com.artemonre.onemoretodolist.feature.todolist.presentation

sealed interface TodoListEvent {
    data object ShowAddTodoSheet : TodoListEvent
    data object ShowAddTodoFullScreenDialog : TodoListEvent
    data class ShowEditTodoSheet(val item: TodoItemUi) : TodoListEvent
}
