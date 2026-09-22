package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class HandleDueTodoFiredTest {

    @Test
    fun `firing clears the due time and promotes the todo to the top`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(dueTodo(id = "1", sortOrder = 0), dueTodo(id = "2", sortOrder = 1))
        )
        val handleDueTodoFired = HandleDueTodoFired(dataSource)

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
        val handleDueTodoFired = HandleDueTodoFired(dataSource)

        assertNull(handleDueTodoFired("missing"))
    }

    @Test
    fun `returns null if the due time was cleared before firing`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoWithoutDueTime(id = "1")))
        val handleDueTodoFired = HandleDueTodoFired(dataSource)

        assertNull(handleDueTodoFired("1"))
    }

    @Test
    fun `returns null if the todo was already completed before firing`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(dueTodo(id = "1", status = TodoStatus.Done)))
        val handleDueTodoFired = HandleDueTodoFired(dataSource)

        assertNull(handleDueTodoFired("1"))
    }

    private fun dueTodo(
        id: String,
        sortOrder: Int = 0,
        status: TodoStatus = TodoStatus.Active
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = sortOrder,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        dueDate = LocalDate(2026, 6, 1),
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
