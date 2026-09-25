package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toInstant

class ToggleTodoDoneTest {

    @Test
    fun `completing a recurring todo marks it done without spawning a copy`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence)))
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())

        toggleTodoDone("1")

        val todos = dataSource.observeTodos().first()
        assertEquals(1, todos.size)
        assertEquals(TodoStatus.Done, todos.single().status)
        assertEquals(recurrence, todos.single().recurrence)
    }

    @Test
    fun `a recurring todo is never immediately deleted even when archiving is off`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 2, unit = RecurrenceUnit.Day)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence)))
        val todoPreferences = FakeTodoPreferences(initialArchiveCompletedTodos = false)
        val toggleTodoDone = ToggleTodoDone(dataSource, todoPreferences, NoOpDueTimeScheduler())

        toggleTodoDone("1")

        val todos = dataSource.observeTodos().first()
        assertEquals(1, todos.size)
        assertEquals(TodoStatus.Done, todos.single().status)
    }

    @Test
    fun `a non-recurring todo is still deleted immediately when archiving is off`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(recurringTodo(id = "1", recurrence = null)))
        val todoPreferences = FakeTodoPreferences(initialArchiveCompletedTodos = false)
        val toggleTodoDone = ToggleTodoDone(dataSource, todoPreferences, NoOpDueTimeScheduler())

        toggleTodoDone("1")

        assertEquals(emptyList(), dataSource.observeTodos().first())
    }

    @Test
    fun `toggling a recurring todo back to active clears its completion date`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Month)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence, status = TodoStatus.Done))
        )
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())

        toggleTodoDone("1")

        val todos = dataSource.observeTodos().first()
        assertEquals(1, todos.size)
        assertEquals(TodoStatus.Active, todos.single().status)
        assertNull(todos.single().completionDate)
    }

    @Test
    fun `completing an AfterCompletion todo sets its anchor to now`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 2, unit = RecurrenceUnit.Day)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence)))
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())

        toggleTodoDone("1")

        assertNotNull(dataSource.observeTodos().first().single().recurrenceAnchorInstant)
    }

    @Test
    fun `completing an Every todo does not touch its anchor`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence))
        )
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())

        toggleTodoDone("1")

        assertNull(dataSource.observeTodos().first().single().recurrenceAnchorInstant)
    }

    @Test
    fun `completing an AfterCompletion todo with a due time schedules the next occurrence at the same time of day`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 3, unit = RecurrenceUnit.Day)
        val dueTime = LocalTime(9, 30)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                recurringTodo(id = "1", recurrence = recurrence, dueTime = dueTime, dueTimeMode = DueTimeMode.Exact)
            )
        )
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())

        toggleTodoDone("1")

        val updated = dataSource.observeTodos().first().single()
        val zone = TimeZone.currentSystemDefault()
        val today = Clock.System.todayIn(zone)
        val expectedDate = recurrence.nextDate(LocalDateTime(today, dueTime).toInstant(zone))
        assertEquals(expectedDate, updated.dueDate)
        assertEquals(dueTime, updated.dueTime)
        assertEquals(DueTimeMode.Exact, updated.dueTimeMode)
    }

    @Test
    fun `completing an AfterCompletion todo with no due time leaves it untouched`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 2, unit = RecurrenceUnit.Day)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence)))
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())

        toggleTodoDone("1")

        val updated = dataSource.observeTodos().first().single()
        assertNull(updated.dueDate)
        assertNull(updated.dueTime)
        assertNull(updated.dueTimeMode)
    }

    @Test
    fun `completing an Every todo with a due time leaves its due date and time untouched`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val dueDate = LocalDate(2026, 6, 1)
        val dueTime = LocalTime(10, 0)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                recurringTodo(id = "1", recurrence = recurrence, dueDate = dueDate, dueTime = dueTime)
            )
        )
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences(), NoOpDueTimeScheduler())

        toggleTodoDone("1")

        val updated = dataSource.observeTodos().first().single()
        assertEquals(dueDate, updated.dueDate)
        assertEquals(dueTime, updated.dueTime)
    }

    private fun recurringTodo(
        id: String,
        recurrence: Recurrence?,
        status: TodoStatus = TodoStatus.Active,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        dueTimeMode: DueTimeMode? = null
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        recurrence = recurrence,
        dueDate = dueDate,
        dueTime = dueTime,
        dueTimeMode = dueTimeMode
    )
}
