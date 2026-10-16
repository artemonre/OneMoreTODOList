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
    // deleted between when the alarm was armed and when it actually fired. A recurring todo is the
    // one legitimate exception to "must be Active": completing one keeps its next occurrence armed
    // (see ToggleTodoDone), so Done is exactly the expected state for its alarm to fire in - that's
    // what reactivates it, precisely on time, instead of waiting on ApplyDueRecurrences' own
    // coarser periodic catch-up.
    suspend operator fun invoke(todoId: String): TodoItem? {
        val currentTodos = dataSource.observeTodos().first()
        val item = currentTodos.firstOrNull { it.id == todoId } ?: return null
        val recurrence = item.recurrence
        if (item.status != TodoStatus.Active && recurrence == null) return null
        val dueInstant = item.dueInstant()
        // The one per-todo alarm is armed for whichever comes first (see nextAlarmInstant), so a
        // snooze at or before the due time means this firing is the snooze. It only re-notifies
        // and moves the todo to the top - recurrence and due fields stay untouched, and the
        // reschedule below re-arms the regular due time, if there is one.
        val snoozedUntil = item.snoozedUntil
        if (snoozedUntil != null && (dueInstant == null || snoozedUntil <= dueInstant)) {
            val updated = item.copy(
                snoozedUntil = null,
                sortOrder = topSortOrder(currentTodos),
                priorityOrder = topPriorityOrder(currentTodos)
            )
            dataSource.upsertTodo(updated)
            dueTimeScheduler.reschedule(updated)
            return updated
        }
        if (dueInstant == null) return null
        val updated = recurrence?.let { item.reachedDueOccurrence(it, currentTodos, Clock.System.now()) }
            ?: item.copy(
                dueDate = null,
                dueTime = null,
                dueTimeMode = null,
                sortOrder = topSortOrder(currentTodos),
                priorityOrder = topPriorityOrder(currentTodos)
            )
        dataSource.upsertTodo(updated)
        dueTimeScheduler.reschedule(updated)
        return updated
    }
}
