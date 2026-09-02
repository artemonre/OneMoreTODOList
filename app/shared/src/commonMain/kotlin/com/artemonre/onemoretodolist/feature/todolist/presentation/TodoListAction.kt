package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption

sealed interface TodoListAction {
    data class OnToggleDone(val id: String) : TodoListAction
    data class OnSortOptionSelected(val option: TodoSortOption) : TodoListAction
    data class OnReorder(val orderedIds: List<String>) : TodoListAction
    data object OnAddTodoClick : TodoListAction
    data class OnConfirmAddTodo(val text: String, val isPrioritized: Boolean) : TodoListAction
    data class OnEditTodoClick(val id: String) : TodoListAction
    data class OnConfirmEditTodo(val id: String, val text: String, val isPrioritized: Boolean) : TodoListAction
    data class OnDeleteTodo(val id: String) : TodoListAction
}
