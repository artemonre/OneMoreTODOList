package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption

sealed interface TodoListAction {
    data class OnTodoClick(val id: String) : TodoListAction
    data class OnToggleDone(val id: String) : TodoListAction
    data class OnSortOptionSelected(val option: TodoSortOption) : TodoListAction
    data object OnAddTodoClick : TodoListAction
    data class OnConfirmAddTodo(val title: String, val isPrioritized: Boolean) : TodoListAction
}
