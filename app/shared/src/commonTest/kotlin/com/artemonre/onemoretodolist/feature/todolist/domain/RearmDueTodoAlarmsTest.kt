package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class RearmDueTodoAlarmsTest {

    @Test
    fun `reschedules every active todo with a due time`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(dueTodo(id = "1"), dueTodo(id = "2")))
        val scheduler = FakeDueTimeScheduler()
        val rearmDueTodoAlarms = RearmDueTodoAlarms(dataSource, scheduler)

        rearmDueTodoAlarms()

        assertEquals(listOf("1", "2"), scheduler.rescheduledIds.sorted())
    }

    @Test
    fun `skips active todos without a due time`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoWithoutDueTime(id = "1")))
        val scheduler = FakeDueTimeScheduler()
        val rearmDueTodoAlarms = RearmDueTodoAlarms(dataSource, scheduler)

        rearmDueTodoAlarms()

        assertEquals(emptyList(), scheduler.rescheduledIds)
    }

    @Test
    fun `skips done todos even if they still carry a due time`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(dueTodo(id = "1", status = TodoStatus.Done)))
        val scheduler = FakeDueTimeScheduler()
        val rearmDueTodoAlarms = RearmDueTodoAlarms(dataSource, scheduler)

        rearmDueTodoAlarms()

        assertEquals(emptyList(), scheduler.rescheduledIds)
    }

    // Regression guard for the forward-timezone-jump fix: a due todo must still be rearmed even
    // when its wall-clock time has already passed, so AlarmManager can fire it immediately instead
    // of it being silently dropped - see RearmDueTodoAlarms' own comment.
    @Test
    fun `reschedules a due todo whose time has already passed, instead of dropping it`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(dueTodo(id = "1", dueDate = LocalDate(2000, 1, 1))))
        val scheduler = FakeDueTimeScheduler()
        val rearmDueTodoAlarms = RearmDueTodoAlarms(dataSource, scheduler)

        rearmDueTodoAlarms()

        assertEquals(listOf("1"), scheduler.rescheduledIds)
    }

    private fun dueTodo(
        id: String,
        status: TodoStatus = TodoStatus.Active,
        dueDate: LocalDate = LocalDate(2026, 6, 1)
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        dueDate = dueDate,
        dueTime = LocalTime(10, 0),
        dueTimeMode = DueTimeMode.Approximate
    )

    private fun todoWithoutDueTime(id: String) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1)
    )
}
