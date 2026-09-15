package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem

fun TodoEntity.toTodoItem(): TodoItem = TodoItem(
    id = id,
    text = text,
    status = status,
    sortOrder = sortOrder,
    creationDate = creationDate,
    lastEditDate = lastEditDate,
    completionDate = completionDate,
    priorityOrder = priorityOrder,
    recurrence = recurrenceType?.let { type ->
        val interval = recurrenceInterval ?: return@let null
        val unit = recurrenceUnit ?: return@let null
        Recurrence(type = type, interval = interval, unit = unit)
    },
    recurrenceAnchorInstant = recurrenceAnchorInstant,
    topSince = topSince
)

fun TodoItem.toTodoEntity(): TodoEntity = TodoEntity(
    id = id,
    text = text,
    status = status,
    sortOrder = sortOrder,
    creationDate = creationDate,
    lastEditDate = lastEditDate,
    completionDate = completionDate,
    priorityOrder = priorityOrder,
    recurrenceType = recurrence?.type,
    recurrenceInterval = recurrence?.interval,
    recurrenceUnit = recurrence?.unit,
    recurrenceAnchorInstant = recurrenceAnchorInstant,
    topSince = topSince
)
