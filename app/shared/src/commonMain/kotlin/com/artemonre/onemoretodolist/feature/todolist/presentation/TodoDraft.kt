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

// Nothing typed or picked yet - not worth keeping as an unsaved add draft.
internal val TodoDraft.isEmpty: Boolean
    get() = text.isBlank() && !isPrioritized && recurrence == null && dueDate == null &&
        checklist.all { it.text.isBlank() } && tags.isEmpty()

// Uses something only TodoFormFullScreenDialog shows, so an unsaved add draft has to reopen there -
// the quick-add sheet would hide it.
internal val TodoDraft.needsFullForm: Boolean
    get() = recurrence != null || dueDate != null || checklist.isNotEmpty() || tags.isNotEmpty()

// An existing todo as the form's starting point - TodoFormBody seeds edit and add mode alike from a draft.
internal fun TodoItemUi.toTodoDraft(): TodoDraft = TodoDraft(
    text = text,
    isPrioritized = isPrioritized,
    recurrence = recurrence,
    dueDate = dueDate,
    dueTime = dueTime,
    dueTimeMode = dueTimeMode,
    checklist = checklist,
    tags = tags
)
