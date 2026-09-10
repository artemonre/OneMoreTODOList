package com.artemonre.onemoretodolist.feature.todolist.presentation

data class TodoListOverlayState(
    val showAddTodoSheet: Boolean = false,
    val showAddTodoFullScreenDialog: Boolean = false,
    val editingItem: TodoItemUi? = null,
    val shareItem: TodoItemUi? = null
) {
    val isActive: Boolean
        get() = showAddTodoSheet || showAddTodoFullScreenDialog || editingItem != null || shareItem != null
}
