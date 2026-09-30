package com.artemonre.onemoretodolist.feature.todolist.domain

// One line of a todo's checklist - a single level only, no nesting. Ticking items off is
// independent of the todo's own status: completing every item doesn't complete the todo.
data class ChecklistItem(
    val id: String,
    val text: String,
    val isDone: Boolean = false
)
