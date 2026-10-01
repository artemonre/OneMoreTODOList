package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class TimestampingTodoLocalDataSourceTest {

    private val firstWrite = Instant.fromEpochMilliseconds(1_000)
    private val secondWrite = Instant.fromEpochMilliseconds(2_000)

    @Test
    fun `an edit stamps updatedAt with the current time`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val clock = SteppingClock(firstWrite, secondWrite)
        val stamping = TimestampingTodoLocalDataSource(fake, clock)

        stamping.upsertTodo(todoItem(text = "Before"))
        stamping.upsertTodo(todoItem(text = "After"))

        assertEquals(secondWrite, fake.getAllIncludingDeleted().single().updatedAt)
    }

    @Test
    fun `a topSince-only write keeps the stored updatedAt`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val clock = SteppingClock(firstWrite, secondWrite)
        val stamping = TimestampingTodoLocalDataSource(fake, clock)

        stamping.upsertTodo(todoItem())
        val stored = fake.getAllIncludingDeleted().single()
        stamping.upsertTodo(stored.copy(topSince = Instant.fromEpochMilliseconds(5_000)))

        assertEquals(firstWrite, fake.getAllIncludingDeleted().single().updatedAt)
    }

    @Test
    fun `upsertVerbatim keeps the given timestamps`() = runTest {
        val fake = FakeTodoLocalDataSource()
        val stamping = TimestampingTodoLocalDataSource(fake, SteppingClock(secondWrite))
        val incoming = todoItem().copy(updatedAt = firstWrite)

        stamping.upsertVerbatim(listOf(incoming))

        assertEquals(firstWrite, fake.getAllIncludingDeleted().single().updatedAt)
    }

    private fun todoItem(text: String = "Todo") = TodoItem(
        id = "1",
        text = text,
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1)
    )
}

// Returns the given instants in order, one per now() call, repeating the last one after that.
private class SteppingClock(vararg instants: Instant) : Clock {
    private val remaining = ArrayDeque(instants.toList())
    private var last = instants.last()

    override fun now(): Instant = remaining.removeFirstOrNull()?.also { last = it } ?: last
}
