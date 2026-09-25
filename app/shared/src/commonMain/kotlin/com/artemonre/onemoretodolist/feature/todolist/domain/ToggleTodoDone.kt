package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toInstant

// Framework-free (no Android/Compose/Glance) so both the todo list screen and the Android
// home-screen widget's checkbox rows share one "toggle done" implementation.
class ToggleTodoDone(
    private val dataSource: TodoLocalDataSource,
    private val todoPreferences: TodoPreferences,
    private val dueTimeScheduler: DueTimeScheduler
) {
    // allowImmediateDelete lets the widget's checkbox opt out of the archive-off delete branch -
    // a home-screen mis-tap has no Snackbar/undo surface available (Glance has no equivalent),
    // unlike the in-app checkbox, which captures the item for undo before calling this. A
    // recurring todo is never immediately deleted this way regardless - it needs to survive as
    // Done so ApplyDueRecurrences can bring it back later; only an explicit delete removes it.
    suspend operator fun invoke(id: String, allowImmediateDelete: Boolean = true) {
        val item = dataSource.observeTodos().first().firstOrNull { it.id == id } ?: return
        val canImmediatelyDelete = item.status == TodoStatus.Active &&
            allowImmediateDelete &&
            item.recurrence == null &&
            !todoPreferences.archiveCompletedTodos.first()
        if (canImmediatelyDelete) {
            dataSource.deleteTodo(id)
            dueTimeScheduler.cancel(id)
            return
        }
        val newStatus = item.status.toggled()
        val completionDate = if (newStatus == TodoStatus.Done) {
            Clock.System.todayIn(TimeZone.currentSystemDefault())
        } else {
            null
        }
        // AfterCompletion counts its delay from the moment it's actually completed, so this is
        // where its cycle starts - Every's own anchor is only ever touched by ApplyDueRecurrences.
        val recurrenceAnchorInstant = if (newStatus == TodoStatus.Done && item.recurrence?.type == RecurrenceType.AfterCompletion) {
            Clock.System.now()
        } else {
            item.recurrenceAnchorInstant
        }
        // A one-shot todo (no recurrence) that becomes Done is no longer active/due - one-way
        // reset, same as any other "archive" of it. Toggling back to Active does not restore a due
        // time. A recurring todo instead keeps or advances its due time so the next occurrence
        // still notifies - see the per-type handling below.
        val zone = TimeZone.currentSystemDefault()
        val (dueDate, dueTime, dueTimeMode) = when {
            newStatus != TodoStatus.Done -> Triple(item.dueDate, item.dueTime, item.dueTimeMode)
            item.recurrence == null -> Triple(null, null, null)
            // Recurs on its own fixed schedule independent of completion (see
            // ApplyDueRecurrences/HandleDueTodoFired) - completing it early shouldn't cancel its
            // already-armed next alarm.
            item.recurrence.type == RecurrenceType.Every -> Triple(item.dueDate, item.dueTime, item.dueTimeMode)
            // The dueTime template survived HandleDueTodoFired clearing only dueDate on the last
            // fire (or was never touched yet) - combine it with today to compute the next
            // occurrence, N (interval) after this completion, at the same time of day.
            item.recurrence.type == RecurrenceType.AfterCompletion && completionDate != null && item.dueTime != null -> {
                val nextDate = item.recurrence.nextDate(
                    LocalDateTime(completionDate, item.dueTime).toInstant(zone)
                )
                Triple(nextDate, item.dueTime, item.dueTimeMode)
            }
            // AfterCompletion with no due time ever set - pure interval recurrence, no
            // notification, left to ApplyDueRecurrences as before.
            else -> Triple(null, null, null)
        }
        val updated = item.copy(
            status = newStatus,
            completionDate = completionDate,
            recurrenceAnchorInstant = recurrenceAnchorInstant,
            dueDate = dueDate,
            dueTime = dueTime,
            dueTimeMode = dueTimeMode
        )
        dataSource.upsertTodo(updated)
        dueTimeScheduler.reschedule(updated)
    }
}
