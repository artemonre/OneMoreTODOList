package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class ToggleTodoDoneTest {

    @Test
    fun `completing a recurring todo marks it done without spawning a copy`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence)))
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences())

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
        val toggleTodoDone = ToggleTodoDone(dataSource, todoPreferences)

        toggleTodoDone("1")

        val todos = dataSource.observeTodos().first()
        assertEquals(1, todos.size)
        assertEquals(TodoStatus.Done, todos.single().status)
    }

    @Test
    fun `a non-recurring todo is still deleted immediately when archiving is off`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(recurringTodo(id = "1", recurrence = null)))
        val todoPreferences = FakeTodoPreferences(initialArchiveCompletedTodos = false)
        val toggleTodoDone = ToggleTodoDone(dataSource, todoPreferences)

        toggleTodoDone("1")

        assertEquals(emptyList(), dataSource.observeTodos().first())
    }

    @Test
    fun `toggling a recurring todo back to active clears its completion date`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Month)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence, status = TodoStatus.Done))
        )
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences())

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
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences())

        toggleTodoDone("1")

        assertNotNull(dataSource.observeTodos().first().single().recurrenceAnchorInstant)
    }

    @Test
    fun `completing an Every todo does not touch its anchor`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(recurringTodo(id = "1", recurrence = recurrence))
        )
        val toggleTodoDone = ToggleTodoDone(dataSource, FakeTodoPreferences())

        toggleTodoDone("1")

        assertNull(dataSource.observeTodos().first().single().recurrenceAnchorInstant)
    }

    private fun recurringTodo(
        id: String,
        recurrence: Recurrence?,
        status: TodoStatus = TodoStatus.Active
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        recurrence = recurrence
    )
}
