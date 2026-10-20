package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

// How long past its due time a recurring todo's own alarm gets to fire (an approximate window,
// Doze deferral) before this catch-up treats it as missed and applies the occurrence itself -
// without this, an hourly run landing just after the due time would beat the alarm to it and the
// occurrence would come back silently, with no notification.
private val MISSED_ALARM_GRACE = 1.hours

// Framework-free (no Android/Compose/Glance) so it can run both at app startup (ContainerViewModel)
// and at the start of the widget's own composition (TodoWidget.provideGlance) - the two places a
// recurring todo's due date might need to be caught up without either one being open all the time.
//
// A recurring todo is never a copy: the same id/creationDate gets pulled back to Active and the
// top whenever it's due, regardless of whether it's currently Active or Done - it keeps repeating
// until the todo itself is deleted. A todo with a due time follows that due time alone - its alarm
// (HandleDueTodoFired) is what brings it back, and this only catches up a missed one. Without a
// due time, Every-type todos fire on a fixed schedule (independent of completion) and
// AfterCompletion-type todos only once actually completed, interval/unit later.
class ApplyDueRecurrences(
    private val dataSource: TodoLocalDataSource,
    private val dueTimeScheduler: DueTimeScheduler
) {
    suspend operator fun invoke() {
        val now = Clock.System.now()
        var todos = dataSource.observeTodos().first()
        val dueIds = todos.filter { isDue(it, now) }.map { it.id }

        for (id in dueIds) {
            val item = todos.first { it.id == id }
            val recurrence = item.recurrence ?: continue
            val hasDueTime = item.dueInstant() != null
            val updated = if (hasDueTime) {
                item.reachedDueOccurrence(recurrence, todos, now)
            } else {
                item.copy(
                    status = TodoStatus.Active,
                    completionDate = null,
                    sortOrder = topSortOrder(todos),
                    priorityOrder = topPriorityOrder(todos),
                    recurrenceAnchorInstant = if (recurrence.type == RecurrenceType.Every) now else item.recurrenceAnchorInstant
                )
            }
            dataSource.upsertTodo(updated)
            // Replaces the missed alarm with the next occurrence's (or cancels it, for AfterCompletion).
            if (hasDueTime) dueTimeScheduler.reschedule(updated)
            // Keeps sortOrder/priorityOrder stacking correctly if several todos come due in the
            // same pass, instead of every one of them tying for the exact same "top" spot.
            todos = todos.map { if (it.id == id) updated else it }
        }
    }

    private fun isDue(item: TodoItem, now: Instant): Boolean {
        val recurrence = item.recurrence ?: return false
        item.dueInstant()?.let { return it + MISSED_ALARM_GRACE <= now }
        return when (recurrence.type) {
            RecurrenceType.Every -> {
                val anchor = item.recurrenceAnchorInstant
                    ?: item.creationDate.atStartOfDayIn(TimeZone.currentSystemDefault())
                anchor + recurrence.duration() <= now
            }
            RecurrenceType.AfterCompletion -> {
                if (item.status != TodoStatus.Done) return false
                val completedAt = item.recurrenceAnchorInstant ?: return false
                completedAt + recurrence.duration() <= now
            }
        }
    }
}
