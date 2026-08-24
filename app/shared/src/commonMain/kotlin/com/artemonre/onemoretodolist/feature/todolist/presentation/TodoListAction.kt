package com.artemonre.onemoretodolist.feature.todolist.presentation

sealed interface TodoListAction {
    data class OnTodoClick(val id: String) : TodoListAction
    data class OnToggleDone(val id: String) : TodoListAction
}
