package com.artemonre.onemoretodolist.feature.todolist.presentation

data class TodoListState(
    val items: List<TodoItemUi> = emptyList()
)
