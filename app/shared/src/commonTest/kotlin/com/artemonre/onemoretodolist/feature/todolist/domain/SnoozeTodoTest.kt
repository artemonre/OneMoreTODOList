package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class SnoozeTodoTest {

    private val now = Instant.parse("2026-09-30T10:15:00Z")

    @Test
    fun `one hour snooze is exactly an hour from now`() {
        assertEquals(now + 1.hours, SnoozeOption.OneHour.snoozeUntil(now, TimeZone.UTC))
    }

    @Test
    fun `tomorrow snooze keeps the same wall-clock time across a DST change`() {
        // Berlin leaves DST on 2026-10-25 - "tomorrow at 10:15" is 25 hours later, not 24.
        val zone = TimeZone.of("Europe/Berlin")
        val before = LocalDateTime(2026, 10, 24, 10, 15).toInstant(zone)

        val until = SnoozeOption.Tomorrow.snoozeUntil(before, zone)

        assertEquals(LocalDateTime(2026, 10, 25, 10, 15), until.toLocalDateTime(zone))
    }

    @Test
    fun `snoozing sets snoozedUntil, leaves the due time alone, and reschedules`() = runTest {
        val todo = todoItem(id = "1", dueDate = LocalDate(2026, 10, 3), dueTime = LocalTime(9, 0))
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todo))
        val scheduler = FakeDueTimeScheduler()

        SnoozeTodo(dataSource, scheduler)("1", SnoozeOption.OneHour, now)

        val persisted = dataSource.observeTodos().first().single()
        assertEquals(now + 1.hours, persisted.snoozedUntil)
        assertEquals(todo.dueDate, persisted.dueDate)
        assertEquals(todo.dueTime, persisted.dueTime)
        assertEquals(listOf("1"), scheduler.rescheduledIds)
    }

    @Test
    fun `snoozing a todo completed in the meantime does nothing`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", status = TodoStatus.Done)))
        val scheduler = FakeDueTimeScheduler()

        SnoozeTodo(dataSource, scheduler)("1", SnoozeOption.OneHour, now)

        assertNull(dataSource.observeTodos().first().single().snoozedUntil)
        assertEquals(emptyList(), scheduler.rescheduledIds)
    }

    @Test
    fun `completing a snoozed todo clears the snooze`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1").copy(snoozedUntil = now + 1.hours)))

        ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())("1")

        assertNull(dataSource.observeTodos().first().single().snoozedUntil)
    }

    @Test
    fun `the alarm is armed for the earlier of due time and snooze`() {
        val zone = TimeZone.UTC
        val due = todoItem(id = "1", dueDate = LocalDate(2026, 9, 30), dueTime = LocalTime(18, 0))

        assertEquals(now + 1.hours, due.copy(snoozedUntil = now + 1.hours).nextAlarmInstant(zone))
        assertEquals(
            LocalDateTime(2026, 9, 30, 18, 0).toInstant(zone),
            due.copy(snoozedUntil = now + 24.hours).nextAlarmInstant(zone)
        )
    }

    private fun todoItem(
        id: String,
        status: TodoStatus = TodoStatus.Active,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        dueDate = dueDate,
        dueTime = dueTime,
        dueTimeMode = if (dueDate != null) DueTimeMode.Approximate else null
    )
}
