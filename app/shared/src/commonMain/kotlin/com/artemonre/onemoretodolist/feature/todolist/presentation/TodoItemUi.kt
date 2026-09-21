package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TopTodoAttention
import com.artemonre.onemoretodolist.feature.todolist.domain.daysAtTop
import com.artemonre.onemoretodolist.feature.todolist.domain.topTodoAttention
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

data class TodoItemUi(
    val id: String,
    val text: String,
    val status: TodoStatus,
    val sortOrder: Int,
    val formattedDate: String,
    val isPrioritized: Boolean = false,
    val recurrence: Recurrence? = null,
    val attention: TopTodoAttention = TopTodoAttention.None,
    val daysAtTop: Long? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val dueTimeMode: DueTimeMode? = null
)

fun TodoItem.toTodoItemUi(): TodoItemUi = TodoItemUi(
    id = id,
    text = text,
    status = status,
    sortOrder = sortOrder,
    formattedDate = dateFormat.format(creationDate),
    isPrioritized = priorityOrder != null,
    recurrence = recurrence,
    attention = topTodoAttention(topSince),
    daysAtTop = daysAtTop(topSince),
    dueDate = dueDate,
    dueTime = dueTime,
    dueTimeMode = dueTimeMode
)

private val dateFormat = LocalDate.Format {
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}
