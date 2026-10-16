package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

enum class RecurrenceType {
    Every,
    AfterCompletion
}

enum class RecurrenceUnit {
    Day,
    Week,
    Month,
    Year
}

data class Recurrence(
    val type: RecurrenceType,
    val interval: Int,
    val unit: RecurrenceUnit
)

private fun RecurrenceUnit.toDuration(): Duration = when (this) {
    RecurrenceUnit.Day -> 1.days
    RecurrenceUnit.Week -> 7.days
    RecurrenceUnit.Month -> 30.days
    RecurrenceUnit.Year -> 365.days
}

fun Recurrence.duration(): Duration = unit.toDuration() * interval

// Advances a date by one recurrence interval via Instant math - callers keep the original
// LocalTime fixed rather than deriving it from the shifted instant, which is what pins a chosen
// time of day (e.g. 10am) across every future occurrence instead of letting it drift.
fun Recurrence.nextDate(from: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    (from + duration()).toLocalDateTime(zone).date

// What a recurring todo with a due time becomes once that due time is reached - shared by the alarm
// firing (HandleDueTodoFired) and the catch-up for a missed one (ApplyDueRecurrences), so both land
// on the same state. Either way the reminder is the "it's due again" moment: the todo comes back
// Active at the top, even if it was completed in the meantime.
// - Every advances dueDate to the first occurrence still ahead of now, keeping the time of day -
//   skipping any missed while the phone was off, instead of re-firing once per missed occurrence.
//   recurrenceAnchorInstant is bumped too, for the no-due-time fallback in ApplyDueRecurrences.
// - AfterCompletion clears dueDate alone, leaving nothing armed until it's completed again (see
//   ToggleTodoDone) - dueTime/dueTimeMode stay as the frozen "time of day" template for that.
fun TodoItem.reachedDueOccurrence(
    recurrence: Recurrence,
    currentTodos: List<TodoItem>,
    now: Instant,
    zone: TimeZone = TimeZone.currentSystemDefault()
): TodoItem {
    val reactivated = copy(
        status = TodoStatus.Active,
        completionDate = null,
        sortOrder = topSortOrder(currentTodos),
        priorityOrder = topPriorityOrder(currentTodos)
    )
    return when (recurrence.type) {
        RecurrenceType.Every -> reactivated.copy(
            dueDate = nextOccurrenceAfter(recurrence, now, zone),
            recurrenceAnchorInstant = now
        )
        RecurrenceType.AfterCompletion -> reactivated.copy(dueDate = null)
    }
}

private fun TodoItem.nextOccurrenceAfter(recurrence: Recurrence, now: Instant, zone: TimeZone): LocalDate? {
    val time = dueTime ?: return dueDate
    var date = dueDate ?: return null
    do {
        date = recurrence.nextDate(LocalDateTime(date, time).toInstant(zone), zone)
    } while (LocalDateTime(date, time).toInstant(zone) <= now)
    return date
}
