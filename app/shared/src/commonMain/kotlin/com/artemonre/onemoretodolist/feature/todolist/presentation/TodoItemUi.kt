package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

data class TodoItemUi(
    val id: String,
    val text: String,
    val status: TodoStatus,
    val sortOrder: Int,
    val formattedDate: String,
    val isPrioritized: Boolean = false
)

fun TodoItem.toTodoItemUi(): TodoItemUi = TodoItemUi(
    id = id,
    text = text,
    status = status,
    sortOrder = sortOrder,
    formattedDate = dateFormat.format(date),
    isPrioritized = priorityOrder != null
)

private val dateFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    chars(", ")
    year()
}
