package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem

fun TodoEntity.toTodoItem(): TodoItem = TodoItem(
    id = id,
    text = text,
    status = status,
    sortOrder = sortOrder,
    creationDate = creationDate,
    lastEditDate = lastEditDate,
    completionDate = completionDate,
    priorityOrder = priorityOrder
)

fun TodoItem.toTodoEntity(): TodoEntity = TodoEntity(
    id = id,
    text = text,
    status = status,
    sortOrder = sortOrder,
    creationDate = creationDate,
    lastEditDate = lastEditDate,
    completionDate = completionDate,
    priorityOrder = priorityOrder
)
