package com.artemonre.onemoretodolist.appfunctions

import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

// A todo's complete new state, in exactly the shape EditTodo takes.
internal data class TodoEditValues(
    val text: String,
    val isPrioritized: Boolean,
    val recurrence: Recurrence?,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val dueTimeMode: DueTimeMode?,
    val checklist: List<ChecklistItem>,
    val tags: List<TodoTag>
)

// editTodo's partial-update rules, kept pure (inputs already parsed/validated) so they're unit
// testable without the AppFunctionService around them: null keeps the current value, a passed list
// replaces the current one, and the clear flags win over any value passed alongside them.
internal fun TodoItem.mergeAgentEdit(
    text: String? = null,
    putToTop: Boolean? = null,
    reminder: LocalDateTime? = null,
    exactReminder: Boolean? = null,
    clearReminder: Boolean = false,
    recurrence: Recurrence? = null,
    clearRecurrence: Boolean = false,
    checklist: List<ChecklistItem>? = null,
    tags: List<TodoTag>? = null
): TodoEditValues {
    val newReminder = reminder.takeUnless { clearReminder }
    if (newReminder != null && status != TodoStatus.Active) {
        throw InvalidAgentInputException("Completed todos can't have a reminder. Reopen it with changeTodoDoneStatus first.")
    }
    val newRecurrence = if (clearRecurrence) null else recurrence ?: this.recurrence
    var dueDate = dueDate
    var dueTime = dueTime
    var dueTimeMode = dueTimeMode
    when {
        clearReminder -> {
            dueDate = null
            dueTime = null
            dueTimeMode = null
        }
        newReminder != null -> {
            dueDate = newReminder.date
            dueTime = newReminder.time
            // A new time keeps the current delivery mode unless one is asked for explicitly.
            dueTimeMode = dueTimeMode(exactReminder ?: (this.dueTimeMode == DueTimeMode.Exact))
        }
        exactReminder != null && dueTime != null -> dueTimeMode = dueTimeMode(exactReminder)
    }
    // A time of day with no date is only meaningful as a repeating todo's template (see TodoItem) -
    // once it no longer repeats, drop it too.
    if (newRecurrence == null && dueDate == null) {
        dueTime = null
        dueTimeMode = null
    }
    return TodoEditValues(
        text = text ?: this.text,
        isPrioritized = putToTop ?: (priorityOrder != null),
        recurrence = newRecurrence,
        dueDate = dueDate,
        dueTime = dueTime,
        dueTimeMode = dueTimeMode,
        checklist = checklist ?: this.checklist,
        tags = tags ?: this.tags
    )
}

internal fun dueTimeMode(exact: Boolean): DueTimeMode = if (exact) DueTimeMode.Exact else DueTimeMode.Approximate
