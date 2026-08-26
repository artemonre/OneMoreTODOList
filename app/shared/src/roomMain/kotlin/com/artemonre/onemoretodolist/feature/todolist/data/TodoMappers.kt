package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem

fun TodoEntity.toTodoItem(): TodoItem = TodoItem(
    id = id,
    title = title,
    status = status,
    sortOrder = sortOrder,
    date = date,
    priorityOrder = priorityOrder
)

fun TodoItem.toTodoEntity(): TodoEntity = TodoEntity(
    id = id,
    title = title,
    status = status,
    sortOrder = sortOrder,
    date = date,
    priorityOrder = priorityOrder
)
