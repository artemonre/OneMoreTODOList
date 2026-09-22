package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class AddTodoTest {

    @Test
    fun `unprioritized todo is appended after the current highest sortOrder`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", sortOrder = 0)))
        val addTodo = AddTodo(dataSource, NoOpDueTimeScheduler())

        addTodo("New todo", isPrioritized = false)

        val added = dataSource.observeTodos().first().first { it.text == "New todo" }
        assertEquals(1, added.sortOrder)
        assertNull(added.priorityOrder)
    }

    @Test
    fun `prioritized todo gets the lowest sortOrder, leading Manual sort too`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(todoItem(id = "1", sortOrder = 0), todoItem(id = "2", sortOrder = 1))
        )
        val addTodo = AddTodo(dataSource, NoOpDueTimeScheduler())

        addTodo("Put to top", isPrioritized = true)

        val todos = dataSource.observeTodos().first()
        val added = todos.first { it.text == "Put to top" }
        assertTrue(added.sortOrder < todos.filterNot { it.id == added.id }.minOf { it.sortOrder })
    }

    @Test
    fun `an Every recurrence is carried over and anchored to today`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = emptyList())
        val addTodo = AddTodo(dataSource, NoOpDueTimeScheduler())
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 2, unit = RecurrenceUnit.Week)

        addTodo("Water the plants", isPrioritized = true, recurrence = recurrence)

        val added = dataSource.observeTodos().first().first { it.text == "Water the plants" }
        assertEquals(recurrence, added.recurrence)
        assertNotNull(added.recurrenceAnchorInstant)
    }

    @Test
    fun `an AfterCompletion recurrence is carried over without an anchor instant`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = emptyList())
        val addTodo = AddTodo(dataSource, NoOpDueTimeScheduler())
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 3, unit = RecurrenceUnit.Day)

        addTodo("Refill water filter", isPrioritized = false, recurrence = recurrence)

        val added = dataSource.observeTodos().first().first { it.text == "Refill water filter" }
        assertEquals(recurrence, added.recurrence)
        assertNull(added.recurrenceAnchorInstant)
    }

    private fun todoItem(id: String, sortOrder: Int) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = sortOrder,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1)
    )
}
