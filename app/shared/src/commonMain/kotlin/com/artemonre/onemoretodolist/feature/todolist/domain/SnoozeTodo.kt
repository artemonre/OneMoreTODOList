package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

enum class SnoozeOption { OneHour, Tomorrow }

// "Tomorrow" is the same wall-clock time tomorrow, not now + 24h, so a DST change overnight
// doesn't shift the reminder by an hour.
fun SnoozeOption.snoozeUntil(now: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): Instant = when (this) {
    SnoozeOption.OneHour -> now + 1.hours
    SnoozeOption.Tomorrow -> {
        val local = now.toLocalDateTime(zone)
        LocalDateTime(local.date.plus(1, DateTimeUnit.DAY), local.time).toInstant(zone)
    }
}

// Framework-free - backs the due notification's snooze actions. Sets a one-off reminder via
// snoozedUntil without touching dueDate/dueTime/recurrence (see TodoItem.snoozedUntil).
class SnoozeTodo(
    private val dataSource: TodoLocalDataSource,
    private val dueTimeScheduler: DueTimeScheduler
) {
    suspend operator fun invoke(todoId: String, option: SnoozeOption, now: Instant = Clock.System.now()) {
        val item = dataSource.observeTodos().first().firstOrNull { it.id == todoId } ?: return
        // Completed or deleted between the notification being posted and the tap - nothing to
        // remind about anymore.
        if (item.status != TodoStatus.Active) return
        val updated = item.copy(snoozedUntil = option.snoozeUntil(now))
        dataSource.upsertTodo(updated)
        dueTimeScheduler.reschedule(updated)
    }
}
