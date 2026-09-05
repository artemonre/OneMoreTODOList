package com.artemonre.onemoretodolist.feature.todolist.domain

enum class TodoStatus {
    Active,
    Done
}

fun TodoStatus.toggled(): TodoStatus = when (this) {
    TodoStatus.Active -> TodoStatus.Done
    TodoStatus.Done -> TodoStatus.Active
}
