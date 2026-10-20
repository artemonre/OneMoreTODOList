package com.artemonre.onemoretodolist.feature.todolist.domain

// The most items one todo's checklist can hold - the form stops offering "Add item" at this many, and
// a checklist sent by an assistant is cut down to it. Checklists already longer than this are left
// as they are, just not grown further.
const val MAX_CHECKLIST_ITEMS = 10

// One line of a todo's checklist - a single level only, no nesting. Ticking items off is
// independent of the todo's own status: completing every item doesn't complete the todo.
data class ChecklistItem(
    val id: String,
    val text: String,
    val isDone: Boolean = false
)
