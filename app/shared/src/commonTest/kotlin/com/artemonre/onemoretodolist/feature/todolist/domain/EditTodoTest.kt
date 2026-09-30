package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class EditTodoTest {

    private val anchor = Instant.parse("2026-09-01T10:00:00Z")
    private val weekly = Recurrence(RecurrenceType.Every, 1, RecurrenceUnit.Week)

    @Test
    fun `replaces text, checklist, tags and due time, then reschedules`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem("1")))
        val scheduler = FakeDueTimeScheduler()
        val checklist = listOf(ChecklistItem("a", "Step", isDone = true))
        val tags = listOf(TodoTag("Work", TagColor.Blue))

        val updated = edit(
            dataSource, scheduler, "1",
            text = "New text",
            dueDate = LocalDate(2026, 10, 3),
            dueTime = LocalTime(9, 30),
            dueTimeMode = DueTimeMode.Exact,
            checklist = checklist,
            tags = tags
        )

        assertEquals("New text", updated?.text)
        assertEquals(checklist, updated?.checklist)
        assertEquals(tags, updated?.tags)
        assertEquals(LocalTime(9, 30), updated?.dueTime)
        assertEquals(DueTimeMode.Exact, updated?.dueTimeMode)
        assertEquals(updated, dataSource.observeTodos().first().single())
        assertEquals(listOf("1"), scheduler.rescheduledIds)
    }

    @Test
    fun `blank text keeps the old text`() = runTest {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem("1")))

        assertEquals("Todo 1", edit(dataSource, NoOpDueTimeScheduler(), "1", text = "  ")?.text)
    }

    @Test
    fun `becoming prioritized moves to the top, staying prioritized keeps the position`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                todoItem("1", sortOrder = 0),
                todoItem("2", sortOrder = 5).copy(priorityOrder = 0.5)
            )
        )

        val becoming = edit(dataSource, NoOpDueTimeScheduler(), "1", isPrioritized = true)
        val staying = edit(dataSource, NoOpDueTimeScheduler(), "2", isPrioritized = true)

        assertEquals(-1, becoming?.sortOrder)
        assertNotNull(becoming?.priorityOrder)
        assertEquals(5, staying?.sortOrder)
        assertEquals(0.5, staying?.priorityOrder)
    }

    @Test
    fun `an unchanged Every rule keeps its anchor, a changed one restarts it`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(todoItem("1").copy(recurrence = weekly, recurrenceAnchorInstant = anchor))
        )

        val unchanged = edit(dataSource, NoOpDueTimeScheduler(), "1", recurrence = weekly)
        val changed = edit(dataSource, NoOpDueTimeScheduler(), "1", recurrence = weekly.copy(interval = 2))

        assertEquals(anchor, unchanged?.recurrenceAnchorInstant)
        assertNotEquals(anchor, changed?.recurrenceAnchorInstant)
    }

    @Test
    fun `removing the recurrence clears the anchor`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(todoItem("1").copy(recurrence = weekly, recurrenceAnchorInstant = anchor))
        )

        val updated = edit(dataSource, NoOpDueTimeScheduler(), "1", recurrence = null)

        assertNull(updated?.recurrence)
        assertNull(updated?.recurrenceAnchorInstant)
    }

    @Test
    fun `returns null for a missing todo`() = runTest {
        assertNull(edit(FakeTodoLocalDataSource(), NoOpDueTimeScheduler(), "missing"))
    }

    private suspend fun edit(
        dataSource: TodoLocalDataSource,
        scheduler: DueTimeScheduler,
        id: String,
        text: String = "Todo $id",
        isPrioritized: Boolean = false,
        recurrence: Recurrence? = null,
        dueDate: LocalDate? = null,
        dueTime: LocalTime? = null,
        dueTimeMode: DueTimeMode? = null,
        checklist: List<ChecklistItem> = emptyList(),
        tags: List<TodoTag> = emptyList()
    ) = EditTodo(dataSource, scheduler)(id, text, isPrioritized, recurrence, dueDate, dueTime, dueTimeMode, checklist, tags)

    private fun todoItem(id: String, sortOrder: Int = 0) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = sortOrder,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1)
    )
}
