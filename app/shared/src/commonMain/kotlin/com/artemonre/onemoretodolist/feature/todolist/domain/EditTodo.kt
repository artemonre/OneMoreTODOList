package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// Framework-free - the single "how an existing todo is edited" implementation, shared by the edit
// form (TodoListViewModel) and the on-device agent entry point, so both follow the same ordering,
// recurrence and alarm rules. Takes the todo's complete new state, same as AddTodo; callers that
// only change some fields (the agent's partial edits) merge onto the current todo first.
class EditTodo(
    private val dataSource: TodoLocalDataSource,
    private val dueTimeScheduler: DueTimeScheduler
) {
    // Returns the updated todo, or null if no todo has this id.
    suspend operator fun invoke(
        id: String,
        text: String,
        isPrioritized: Boolean,
        recurrence: Recurrence?,
        dueDate: LocalDate?,
        dueTime: LocalTime?,
        dueTimeMode: DueTimeMode?,
        checklist: List<ChecklistItem>,
        tags: List<TodoTag>
    ): TodoItem? {
        val currentTodos = dataSource.observeTodos().first()
        val item = currentTodos.firstOrNull { it.id == id } ?: return null
        // Only newly-prioritized items jump to the top of Manual sort too - an item that was
        // already prioritized keeps whatever position the user (re)ordered it to.
        val becomingPrioritized = isPrioritized && item.priorityOrder == null
        val updated = item.copy(
            text = text.ifBlank { item.text },
            lastEditDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            sortOrder = if (becomingPrioritized) topSortOrder(currentTodos) else item.sortOrder,
            priorityOrder = if (isPrioritized) {
                item.priorityOrder ?: topPriorityOrder(currentTodos)
            } else {
                null
            },
            recurrence = recurrence,
            recurrenceAnchorInstant = recurrenceAnchorInstant(item, recurrence),
            dueDate = dueDate,
            dueTime = dueTime,
            dueTimeMode = dueTimeMode,
            checklist = checklist,
            tags = tags
        )
        dataSource.upsertTodo(updated)
        dueTimeScheduler.reschedule(updated)
        return updated
    }

    // Every keeps its existing anchor as long as the rule itself is unchanged (same type/interval/
    // unit) - only a genuinely new or changed rule restarts the countdown from now. AfterCompletion
    // always keeps whatever anchor it already had (null if never completed) regardless of edits -
    // its anchor is tied to when it was actually completed, not to the rule, so a changed interval
    // should still count from that same completion instant, not restart it.
    private fun recurrenceAnchorInstant(item: TodoItem, newRecurrence: Recurrence?): Instant? {
        if (newRecurrence?.type != RecurrenceType.Every) {
            return item.recurrenceAnchorInstant.takeIf { newRecurrence?.type == RecurrenceType.AfterCompletion }
        }
        return item.recurrenceAnchorInstant.takeIf { item.recurrence == newRecurrence } ?: Clock.System.now()
    }
}
