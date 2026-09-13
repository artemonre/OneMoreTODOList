package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class ApplyDueRecurrencesTest {

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    @Test
    fun `a due Every todo is pulled to Active and the top, keeping its creation date`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val original = todoItem(
            id = "1",
            status = TodoStatus.Active,
            sortOrder = 5,
            recurrence = recurrence,
            recurrenceAnchorDate = today.minusRecurrence(recurrence)
        )
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(original, todoItem(id = "2", sortOrder = 0))
        )

        ApplyDueRecurrences(dataSource)()

        val todos = dataSource.observeTodos().first()
        val updated = todos.first { it.id == "1" }
        assertEquals(TodoStatus.Active, updated.status)
        assertEquals(original.creationDate, updated.creationDate)
        assertEquals(today, updated.recurrenceAnchorDate)
        assertEquals(updated.sortOrder, todos.minOf { it.sortOrder })
    }

    @Test
    fun `a due Every todo that was completed reactivates and clears its completion date`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Month)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                todoItem(
                    id = "1",
                    status = TodoStatus.Done,
                    completionDate = today,
                    recurrence = recurrence,
                    recurrenceAnchorDate = today.minusRecurrence(recurrence)
                )
            )
        )

        ApplyDueRecurrences(dataSource)()

        val updated = dataSource.observeTodos().first().single()
        assertEquals(TodoStatus.Active, updated.status)
        assertNull(updated.completionDate)
    }

    @Test
    fun `an Every todo not yet due is left untouched`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val original = todoItem(id = "1", sortOrder = 5, recurrence = recurrence, recurrenceAnchorDate = today)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource)()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    @Test
    fun `an AfterCompletion todo reactivates once its delay has passed`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 2, unit = RecurrenceUnit.Day)
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                todoItem(
                    id = "1",
                    status = TodoStatus.Done,
                    completionDate = today.minusRecurrence(recurrence),
                    recurrence = recurrence
                )
            )
        )

        ApplyDueRecurrences(dataSource)()

        val updated = dataSource.observeTodos().first().single()
        assertEquals(TodoStatus.Active, updated.status)
        assertNull(updated.completionDate)
        assertNull(updated.recurrenceAnchorDate)
    }

    @Test
    fun `an AfterCompletion todo that was never completed is left untouched`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(id = "1", status = TodoStatus.Active, recurrence = recurrence)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource)()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    @Test
    fun `a non-recurring todo is left untouched`() = runTest {
        val original = todoItem(id = "1", status = TodoStatus.Done, completionDate = today.minus(5, RecurrenceUnit.Year.toDateTimeUnit()))
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource)()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    private fun LocalDate.minusRecurrence(recurrence: Recurrence): LocalDate =
        minus(recurrence.interval, recurrence.unit.toDateTimeUnit())

    private fun todoItem(
        id: String,
        status: TodoStatus = TodoStatus.Active,
        sortOrder: Int = 0,
        completionDate: LocalDate? = null,
        recurrence: Recurrence? = null,
        recurrenceAnchorDate: LocalDate? = null
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = sortOrder,
        creationDate = today.minus(30, RecurrenceUnit.Day.toDateTimeUnit()),
        lastEditDate = today.minus(30, RecurrenceUnit.Day.toDateTimeUnit()),
        completionDate = completionDate,
        recurrence = recurrence,
        recurrenceAnchorDate = recurrenceAnchorDate
    )
}
