package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

data class TodoItemUi(
    val id: String,
    val title: String,
    val isDone: Boolean,
    val sortOrder: Int,
    val formattedDate: String
)

fun TodoItem.toTodoItemUi(): TodoItemUi = TodoItemUi(
    id = id,
    title = title,
    isDone = isDone,
    sortOrder = sortOrder,
    formattedDate = dateFormat.format(date)
)

private val dateFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    chars(", ")
    year()
}
