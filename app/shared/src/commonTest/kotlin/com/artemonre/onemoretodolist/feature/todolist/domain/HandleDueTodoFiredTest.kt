package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HandleDueTodoFiredTest {

    private val now = Clock.System.now()
    private val zone = TimeZone.currentSystemDefault()

    @Test
    fun `firing clears the due time and promotes the todo to the top`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(dueTodo(id = "1", sortOrder = 0), dueTodo(id = "2", sortOrder = 1))
        )
        val handleDueTodoFired = HandleDueTodoFired(dataSource, NoOpDueTimeScheduler())

        val fired = handleDueTodoFired("1")

        assertEquals("1", fired?.id)
        assertNull(fired?.dueDate)
        assertNull(fired?.dueTime)
        assertNull(fired?.dueTimeMode)
        assertEquals(-1, fired?.sortOrder)
        val persisted = dataSource.observeTodos().first().first { it.id == "1" }
        assertEquals(fired, persisted)
    }

    @Test
    fun `returns null if the todo was deleted before firing`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = emptyList())
        val handleDueTodoFired = HandleDueTodoFired(dataSource, NoOpDueTimeScheduler())

        assertNull(handleDueTodoFired("missing"))
    }

    @Test
    fun `returns null if the due time was cleared before firing`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoWithoutDueTime(id = "1")))
        val handleDueTodoFired = HandleDueTodoFired(dataSource, NoOpDueTimeScheduler())

        assertNull(handleDueTodoFired("1"))
    }

    @Test
    fun `returns null if the todo was already completed before firing`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(dueTodo(id = "1", status = TodoStatus.Done))
        )
        val handleDueTodoFired = HandleDueTodoFired(dataSource, NoOpDueTimeScheduler())

        assertNull(handleDueTodoFired("1"))
    }

    @Test
    fun `firing an Every recurring todo advances to the next occurrence and reschedules`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val original = dueTodo(id = "1", recurrence = recurrence)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))
        val scheduler = FakeDueTimeScheduler()
        val handleDueTodoFired = HandleDueTodoFired(dataSource, scheduler)

        val fired = handleDueTodoFired("1")

        val expectedNextDate = (now + recurrence.duration()).toLocalDateTime(zone).date
        assertEquals(expectedNextDate, fired?.dueDate)
        assertEquals(original.dueTime, fired?.dueTime)
        assertEquals(original.dueTimeMode, fired?.dueTimeMode)
        assertNotNull(fired?.recurrenceAnchorInstant)
        assertEquals(listOf("1"), scheduler.rescheduledIds)
    }

    @Test
    fun `firing an AfterCompletion recurring todo clears the due date but keeps the time of day`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 2, unit = RecurrenceUnit.Day)
        val original = dueTodo(id = "1", recurrence = recurrence)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))
        val handleDueTodoFired = HandleDueTodoFired(dataSource, NoOpDueTimeScheduler())

        val fired = handleDueTodoFired("1")

        assertNull(fired?.dueDate)
        assertEquals(original.dueTime, fired?.dueTime)
        assertEquals(original.dueTimeMode, fired?.dueTimeMode)
    }

    @Test
    fun `firing an AfterCompletion todo that is waiting Done reactivates it instead of no-opping`() = runTest {
        // ToggleTodoDone marks an AfterCompletion todo Done as part of arming its next occurrence -
        // Done is exactly the expected status for this alarm to fire in, not a race to bail out on.
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 2, unit = RecurrenceUnit.Day)
        val original = dueTodo(id = "1", status = TodoStatus.Done, recurrence = recurrence)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))
        val handleDueTodoFired = HandleDueTodoFired(dataSource, NoOpDueTimeScheduler())

        val fired = handleDueTodoFired("1")

        assertEquals(TodoStatus.Active, fired?.status)
        assertNull(fired?.completionDate)
        assertNull(fired?.dueDate)
        assertEquals(original.dueTime, fired?.dueTime)
        assertEquals(original.dueTimeMode, fired?.dueTimeMode)
    }

    private fun dueTodo(
        id: String,
        sortOrder: Int = 0,
        status: TodoStatus = TodoStatus.Active,
        recurrence: Recurrence? = null,
        dueInstant: Instant = now
    ): TodoItem {
        val localDateTime = dueInstant.toLocalDateTime(zone)
        return TodoItem(
            id = id,
            text = "Todo $id",
            status = status,
            sortOrder = sortOrder,
            creationDate = LocalDate(2026, 1, 1),
            lastEditDate = LocalDate(2026, 1, 1),
            recurrence = recurrence,
            dueDate = localDateTime.date,
            dueTime = localDateTime.time,
            dueTimeMode = DueTimeMode.Approximate
        )
    }

    private fun todoWithoutDueTime(id: String) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1)
    )
}
