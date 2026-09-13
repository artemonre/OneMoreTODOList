package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class TopSinceTrackingTodoLocalDataSourceTest {

    @Test
    fun `the only active todo becomes top and gets topSince set`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences())

        tracked.upsertTodo(todoItem(id = "1"))

        assertNotNull(fake.observeTodos().first().single().topSince)
    }

    @Test
    fun `under Date sort an older todo added later outranks and dethrones the current top`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences(initialSortOption = TodoSortOption.Date))

        tracked.upsertTodo(todoItem(id = "1", creationDate = LocalDate(2026, 1, 2)))
        tracked.upsertTodo(todoItem(id = "2", creationDate = LocalDate(2026, 1, 1)))

        val todos = fake.observeTodos().first()
        assertNull(todos.first { it.id == "1" }.topSince)
        assertNotNull(todos.first { it.id == "2" }.topSince)
    }

    @Test
    fun `under Manual sort the lowest sortOrder is top regardless of creation date`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences(initialSortOption = TodoSortOption.Manual))

        tracked.upsertTodo(todoItem(id = "1", sortOrder = 1, creationDate = LocalDate(2026, 1, 1)))
        tracked.upsertTodo(todoItem(id = "2", sortOrder = 0, creationDate = LocalDate(2026, 1, 2)))

        val todos = fake.observeTodos().first()
        assertNull(todos.first { it.id == "1" }.topSince)
        assertNotNull(todos.first { it.id == "2" }.topSince)
    }

    @Test
    fun `under Text sort the alphabetically-first item is top`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences(initialSortOption = TodoSortOption.Text))

        tracked.upsertTodo(todoItem(id = "1", text = "Zebra"))
        tracked.upsertTodo(todoItem(id = "2", text = "Apple"))

        val todos = fake.observeTodos().first()
        assertNull(todos.first { it.id == "1" }.topSince)
        assertNotNull(todos.first { it.id == "2" }.topSince)
    }

    @Test
    fun `Archived preference falls back to Date for top tracking`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences(initialSortOption = TodoSortOption.Archived))

        tracked.upsertTodo(todoItem(id = "1", creationDate = LocalDate(2026, 1, 2)))
        tracked.upsertTodo(todoItem(id = "2", creationDate = LocalDate(2026, 1, 1)))

        val todos = fake.observeTodos().first()
        assertNull(todos.first { it.id == "1" }.topSince)
        assertNotNull(todos.first { it.id == "2" }.topSince)
    }

    @Test
    fun `completing the top todo clears its topSince and promotes the next one`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences())
        tracked.upsertTodo(todoItem(id = "1", creationDate = LocalDate(2026, 1, 1)))
        tracked.upsertTodo(todoItem(id = "2", creationDate = LocalDate(2026, 1, 2)))
        val top = fake.observeTodos().first().first { it.id == "1" }

        tracked.upsertTodo(top.copy(status = TodoStatus.Done))

        val todos = fake.observeTodos().first()
        assertNull(todos.first { it.id == "1" }.topSince)
        assertNotNull(todos.first { it.id == "2" }.topSince)
    }

    @Test
    fun `deleting the top todo promotes the next one`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences())
        tracked.upsertTodo(todoItem(id = "1", creationDate = LocalDate(2026, 1, 1)))
        tracked.upsertTodo(todoItem(id = "2", creationDate = LocalDate(2026, 1, 2)))

        tracked.deleteTodo("1")

        assertNotNull(fake.observeTodos().first().single().topSince)
    }

    @Test
    fun `editing the top todo without changing its rank keeps its original topSince`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val tracked = TopSinceTrackingTodoLocalDataSource(fake, FakeTodoPreferences())
        tracked.upsertTodo(todoItem(id = "1"))
        val originalTopSince = fake.observeTodos().first().single().topSince

        tracked.upsertTodo(fake.observeTodos().first().single().copy(text = "Edited"))

        assertEquals(originalTopSince, fake.observeTodos().first().single().topSince)
    }

    private fun todoItem(
        id: String,
        creationDate: LocalDate = LocalDate(2026, 1, 1),
        status: TodoStatus = TodoStatus.Active,
        sortOrder: Int = 0,
        text: String = "Todo $id"
    ) = TodoItem(
        id = id,
        text = text,
        status = status,
        sortOrder = sortOrder,
        creationDate = creationDate,
        lastEditDate = creationDate
    )
}
