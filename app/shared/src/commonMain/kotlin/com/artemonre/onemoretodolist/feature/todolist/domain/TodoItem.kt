package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.datetime.LocalDate

data class TodoItem(
    val id: String,
    val text: String,
    val status: TodoStatus,
    val sortOrder: Int,
    val creationDate: LocalDate,
    val lastEditDate: LocalDate,
    // Null while Active - set when toggled to Done, cleared when toggled back to Active.
    val completionDate: LocalDate? = null,
    // Rank among other prioritized items; null means not prioritized, lower sorts first.
    // New values are assigned as (currentMin ?: 1.0) * 0.9, always positive, never needing
    // to renumber existing items. Only affects TodoSortOption.Date.
    val priorityOrder: Double? = null,
    // Null means "does not repeat". The SAME todo (same id, unchanged creationDate) gets pulled
    // back to Active and the top whenever it's due - see ApplyDueRecurrences - not a freshly
    // created copy. Keeps repeating until the todo itself is deleted.
    val recurrence: Recurrence? = null,
    // Every-type only: the date its cycle last (re)started counting from. Null while type is
    // AfterCompletion (which anchors off completionDate instead) or there's no recurrence at all.
    val recurrenceAnchorDate: LocalDate? = null
)
