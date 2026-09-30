package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// Everything TodoFormBody collects, handed back in one piece on confirm - add and edit alike.
// Fields a given host doesn't show (e.g. due time in the quick-add sheet) keep their defaults.
data class TodoDraft(
    val text: String,
    val isPrioritized: Boolean,
    val recurrence: Recurrence? = null,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val dueTimeMode: DueTimeMode? = null,
    val checklist: List<ChecklistItem> = emptyList(),
    val tags: List<TodoTag> = emptyList()
)
