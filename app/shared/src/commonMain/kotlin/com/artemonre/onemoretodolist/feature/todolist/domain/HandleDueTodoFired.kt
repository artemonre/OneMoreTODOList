package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlinx.coroutines.flow.first

// Framework-free - invoked by the Android alarm receiver when a due-time alarm fires, but has no
// Android/Compose knowledge itself, same convention as ApplyDueRecurrences.
class HandleDueTodoFired(
    private val dataSource: TodoLocalDataSource,
    private val dueTimeScheduler: DueTimeScheduler
) {
    // Returns the updated item (for the caller to use as notification text), or null if there's
    // nothing to notify about anymore - guards the race where the todo was edited, completed, or
    // deleted between when the alarm was armed and when it actually fired. An AfterCompletion todo
    // is the one legitimate exception to "must be Active": ToggleTodoDone marks it Done as part of
    // arming its next occurrence (see there), so Done is exactly the expected state for its alarm
    // to fire in - that's what reactivates it, precisely on time, instead of waiting on
    // ApplyDueRecurrences' own coarser periodic catch-up.
    suspend operator fun invoke(todoId: String): TodoItem? {
        val currentTodos = dataSource.observeTodos().first()
        val item = currentTodos.firstOrNull { it.id == todoId } ?: return null
        val recurrence = item.recurrence
        val isWaitingAfterCompletion = item.status == TodoStatus.Done && recurrence?.type == RecurrenceType.AfterCompletion
        if (item.status != TodoStatus.Active && !isWaitingAfterCompletion) return null
        val dueInstant = item.dueInstant() ?: return null
        val updated = when (recurrence?.type) {
            // Recurs on its own fixed schedule independent of completion - advance straight to the
            // next occurrence and re-arm, keeping dueTime/dueTimeMode (the time of day) fixed.
            // recurrenceAnchorInstant is bumped too so ApplyDueRecurrences' own fallback check
            // stays in sync instead of re-promoting the item on a separate, creation-date-anchored
            // schedule.
            RecurrenceType.Every -> item.copy(
                dueDate = recurrence.nextDate(dueInstant),
                recurrenceAnchorInstant = Clock.System.now(),
                sortOrder = topSortOrder(currentTodos),
                priorityOrder = topPriorityOrder(currentTodos)
            )
            // Reactivates the todo (a no-op if it was already Active, which is the case the very
            // first time this fires, before it's ever been completed) - this alarm firing is what
            // tells the user it's due again. The next cycle only starts once they complete it again
            // - see ToggleTodoDone - so clear dueDate to leave nothing armed until then, but keep
            // dueTime/dueTimeMode as a frozen "time of day" template for ToggleTodoDone to reuse
            // when it computes that next occurrence.
            RecurrenceType.AfterCompletion -> item.copy(
                status = TodoStatus.Active,
                completionDate = null,
                dueDate = null,
                sortOrder = topSortOrder(currentTodos),
                priorityOrder = topPriorityOrder(currentTodos)
            )
            null -> item.copy(
                dueDate = null,
                dueTime = null,
                dueTimeMode = null,
                sortOrder = topSortOrder(currentTodos),
                priorityOrder = topPriorityOrder(currentTodos)
            )
        }
        dataSource.upsertTodo(updated)
        dueTimeScheduler.reschedule(updated)
        return updated
    }
}
