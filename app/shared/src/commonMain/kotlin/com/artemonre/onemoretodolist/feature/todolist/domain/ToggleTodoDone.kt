package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// Framework-free (no Android/Compose/Glance) so both the todo list screen and the Android
// home-screen widget's checkbox rows share one "toggle done" implementation.
class ToggleTodoDone(
    private val dataSource: TodoLocalDataSource,
    private val todoPreferences: TodoPreferences
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
        dataSource.upsertTodo(
            item.copy(status = newStatus, completionDate = completionDate, recurrenceAnchorInstant = recurrenceAnchorInstant)
        )
    }
}
