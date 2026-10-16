package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ApplyDueRecurrencesTest {

    private val now = Clock.System.now()

    @Test
    fun `a due Every todo is pulled to Active and the top, keeping its creation date`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val original = todoItem(
            id = "1",
            status = TodoStatus.Active,
            sortOrder = 5,
            recurrence = recurrence,
            recurrenceAnchorInstant = now - recurrence.duration()
        )
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(original, todoItem(id = "2", sortOrder = 0))
        )

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        val todos = dataSource.observeTodos().first()
        val updated = todos.first { it.id == "1" }
        assertEquals(TodoStatus.Active, updated.status)
        assertEquals(original.creationDate, updated.creationDate)
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
                    completionDate = today(),
                    recurrence = recurrence,
                    recurrenceAnchorInstant = now - recurrence.duration()
                )
            )
        )

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        val updated = dataSource.observeTodos().first().single()
        assertEquals(TodoStatus.Active, updated.status)
        assertNull(updated.completionDate)
    }

    @Test
    fun `an Every todo not yet due is left untouched`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Week)
        val original = todoItem(id = "1", sortOrder = 5, recurrence = recurrence, recurrenceAnchorInstant = now)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

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
                    completionDate = today(),
                    recurrence = recurrence,
                    recurrenceAnchorInstant = now - recurrence.duration()
                )
            )
        )

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        val updated = dataSource.observeTodos().first().single()
        assertEquals(TodoStatus.Active, updated.status)
        assertNull(updated.completionDate)
    }

    @Test
    fun `an AfterCompletion todo that was never completed is left untouched`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(id = "1", status = TodoStatus.Active, recurrence = recurrence)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    @Test
    fun `an AfterCompletion todo manually reactivated before its delay passes is left untouched`() = runTest {
        // status flipped back to Active by the user, even though the old anchor would otherwise
        // already be "due" - it must not get force-reprocessed since it's not waiting anymore.
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(
            id = "1",
            status = TodoStatus.Active,
            recurrence = recurrence,
            recurrenceAnchorInstant = now - recurrence.duration()
        )
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    @Test
    fun `an Every todo with a due time waits for it, however long ago its anchor was`() = runTest {
        // Daily repeat with the first reminder 10 days out - it must not resurface every day before then.
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(
            id = "1",
            sortOrder = 5,
            recurrence = recurrence,
            recurrenceAnchorInstant = now - 3.days
        ).withDueAt(now + 10.days)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    @Test
    fun `an Every todo whose due time only just passed is left to its own alarm`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(id = "1", status = TodoStatus.Done, completionDate = today(), recurrence = recurrence)
            .withDueAt(now - 5.minutes)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    @Test
    fun `a missed Every due time reactivates the todo and moves on to the next occurrence still ahead`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.Every, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(id = "1", status = TodoStatus.Done, completionDate = today(), recurrence = recurrence)
            .withDueAt(now - 3.days)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))
        val scheduler = FakeDueTimeScheduler()

        ApplyDueRecurrences(dataSource, scheduler)()

        val updated = dataSource.observeTodos().first().single()
        assertEquals(TodoStatus.Active, updated.status)
        assertNull(updated.completionDate)
        assertEquals(original.dueTime, updated.dueTime)
        val nextDue = updated.dueInstant() ?: error("expected a due time")
        assertTrue(nextDue > now && nextDue <= now + 1.days)
        assertEquals(listOf("1"), scheduler.rescheduledIds)
    }

    @Test
    fun `an AfterCompletion todo with a due time waits for it even once its delay has passed`() = runTest {
        // Completed early in the day with the reminder later on - the delay alone mustn't beat the reminder.
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(
            id = "1",
            status = TodoStatus.Done,
            completionDate = today(),
            recurrence = recurrence,
            recurrenceAnchorInstant = now - recurrence.duration()
        ).withDueAt(now + 2.days)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    @Test
    fun `a missed AfterCompletion due time reactivates the todo, keeping only the time of day`() = runTest {
        val recurrence = Recurrence(type = RecurrenceType.AfterCompletion, interval = 1, unit = RecurrenceUnit.Day)
        val original = todoItem(
            id = "1",
            status = TodoStatus.Done,
            completionDate = today(),
            recurrence = recurrence,
            recurrenceAnchorInstant = now - 2.days
        ).withDueAt(now - 1.days)
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))
        val scheduler = FakeDueTimeScheduler()

        ApplyDueRecurrences(dataSource, scheduler)()

        val updated = dataSource.observeTodos().first().single()
        assertEquals(TodoStatus.Active, updated.status)
        assertNull(updated.completionDate)
        assertNull(updated.dueDate)
        assertEquals(original.dueTime, updated.dueTime)
        assertEquals(listOf("1"), scheduler.rescheduledIds)
    }

    @Test
    fun `a non-recurring todo is left untouched`() = runTest {
        val original = todoItem(id = "1", status = TodoStatus.Done, completionDate = today())
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(original))

        ApplyDueRecurrences(dataSource, NoOpDueTimeScheduler())()

        assertEquals(original, dataSource.observeTodos().first().single())
    }

    private fun TodoItem.withDueAt(instant: Instant): TodoItem {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return copy(dueDate = local.date, dueTime = local.time, dueTimeMode = DueTimeMode.Approximate)
    }

    private fun today(): LocalDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

    private fun todoItem(
        id: String,
        status: TodoStatus = TodoStatus.Active,
        sortOrder: Int = 0,
        completionDate: LocalDate? = null,
        recurrence: Recurrence? = null,
        recurrenceAnchorInstant: Instant? = null
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = sortOrder,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        completionDate = completionDate,
        recurrence = recurrence,
        recurrenceAnchorInstant = recurrenceAnchorInstant
    )
}
