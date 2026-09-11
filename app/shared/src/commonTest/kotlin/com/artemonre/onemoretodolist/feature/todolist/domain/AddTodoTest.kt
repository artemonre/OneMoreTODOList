package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class AddTodoTest {

    @Test
    fun `unprioritized todo is appended after the current highest sortOrder`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", sortOrder = 0)))
        val addTodo = AddTodo(dataSource)

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
        val addTodo = AddTodo(dataSource)

        addTodo("Put on top", isPrioritized = true)

        val todos = dataSource.observeTodos().first()
        val added = todos.first { it.text == "Put on top" }
        assertTrue(added.sortOrder < todos.filterNot { it.id == added.id }.minOf { it.sortOrder })
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
