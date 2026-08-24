package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption

data class TodoListState(
    val items: List<TodoItemUi> = emptyList(),
    val sortOption: TodoSortOption = TodoSortOption.Date
)
