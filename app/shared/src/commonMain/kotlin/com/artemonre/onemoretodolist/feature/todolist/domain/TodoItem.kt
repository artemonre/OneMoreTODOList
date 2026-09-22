package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

data class TodoItem(
    val id: String,
    val text: String,
    val status: TodoStatus,
    val sortOrder: Int,
    val creationDate: LocalDate,
    val lastEditDate: LocalDate,
    // Null while Active - set when toggled to Done, cleared when toggled back to Active.
    val completionDate: LocalDate? = null,
    // Rank among other prioritized items; null means not prioritized, lower sorts first.
    // New values are assigned as (currentMin ?: 1.0) * 0.9, always positive, never needing
    // to renumber existing items. Only affects TodoSortOption.Date.
    val priorityOrder: Double? = null,
    // Null means "does not repeat". The SAME todo (same id, unchanged creationDate) gets pulled
    // back to Active and the top whenever it's due - see ApplyDueRecurrences - not a freshly
    // created copy. Keeps repeating until the todo itself is deleted.
    val recurrence: Recurrence? = null,
    // The instant its current cycle started counting from - for Every, set on creation/edit and
    // advanced every time it fires; for AfterCompletion, set by ToggleTodoDone at the moment it's
    // marked Done. Null while there's no recurrence, or an AfterCompletion recurrence that's never
    // been completed yet.
    val recurrenceAnchorInstant: Instant? = null,
    // The instant this todo most recently, continuously became the top of the active list - kept
    // in sync by TopSinceTrackingTodoLocalDataSource after every write. Null while it isn't
    // currently top; see topTodoAttention for how this drives the "long staying" attention color.
    val topSince: Instant? = null,
    // Wall-clock local date/time, NOT a resolved instant - "10am" should still mean 10am local if
    // the timezone changes before it fires (a travelling user, not a fixed-instant appointment).
    // The actual Instant to schedule against is only ever computed on demand (see dueInstant())
    // using whatever timezone is current *at that moment* - see AndroidAlarmDueTimeScheduler. A
    // stale alarm armed against an old zone is only corrected on the next boot or hourly
    // maintenance pass (see RearmDueTodoAlarms), not immediately on a timezone change - deliberate,
    // see RearmDueTodoAlarms' own comment. Null means "no due time set". Cleared (one-shot) the
    // moment the notification fires or the todo is marked Done - see
    // HandleDueTodoFired/ToggleTodoDone. dueTimeMode is null iff dueDate/dueTime are.
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val dueTimeMode: DueTimeMode? = null
)

fun TodoItem.dueInstant(zone: TimeZone = TimeZone.currentSystemDefault()): Instant? {
    val date = dueDate ?: return null
    val time = dueTime ?: return null
    return LocalDateTime(date, time).toInstant(zone)
}
