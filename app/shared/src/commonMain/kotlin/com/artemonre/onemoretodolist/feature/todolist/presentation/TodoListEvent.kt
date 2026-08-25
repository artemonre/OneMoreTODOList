package com.artemonre.onemoretodolist.feature.todolist.presentation

sealed interface TodoListEvent {
    data object ShowAddTodoSheet : TodoListEvent
}
