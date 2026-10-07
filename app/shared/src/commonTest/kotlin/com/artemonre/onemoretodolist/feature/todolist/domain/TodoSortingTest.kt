package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class TodoSortingTest {

    @Test
    fun `Date sort breaks same-day no-priority ties by sortOrder, regardless of input order`() {
        val today = LocalDate(2026, 1, 1)
        val first = todoItem(id = "1", creationDate = today, sortOrder = 0)
        val second = todoItem(id = "2", creationDate = today, sortOrder = 1)
        val third = todoItem(id = "3", creationDate = today, sortOrder = 2)

        // The DB query has no ORDER BY, so the same set of todos can come back in any row order -
        // the displayed order must stay the same regardless.
        val sortedAsInserted = listOf(first, second, third).sortedByOption(TodoSortOption.Date)
        val sortedReversed = listOf(third, second, first).sortedByOption(TodoSortOption.Date)

        assertEquals(listOf("1", "2", "3"), sortedAsInserted.map { it.id })
        assertEquals(listOf("1", "2", "3"), sortedReversed.map { it.id })
    }

    @Test
    fun `Date sort orders same-day todos by createdAt, not by their Manual position`() {
        val today = LocalDate(2026, 1, 1)
        // Dragged under Manual: the older todo now sits second.
        val older = todoItem(id = "older", creationDate = today, sortOrder = 1, createdAtMillis = 1_000)
        val newer = todoItem(id = "newer", creationDate = today, sortOrder = 0, createdAtMillis = 2_000)

        assertEquals(listOf("older", "newer"), listOf(newer, older).sortedByOption(TodoSortOption.Date).map { it.id })
        assertEquals(listOf("newer", "older"), listOf(older, newer).sortedByOption(TodoSortOption.Manual).map { it.id })
    }

    @Test
    fun `legacyCreatedAt keeps the old same-day sortOrder order`() {
        val today = LocalDate(2026, 1, 1)

        assertTrue(legacyCreatedAt(today, -1) < legacyCreatedAt(today, 0))
        assertTrue(legacyCreatedAt(today, 0) < legacyCreatedAt(today, 1))
    }

    private fun todoItem(
        id: String,
        creationDate: LocalDate,
        sortOrder: Int,
        createdAtMillis: Long = 0
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = sortOrder,
        creationDate = creationDate,
        lastEditDate = creationDate,
        createdAt = Instant.fromEpochMilliseconds(createdAtMillis)
    )
}
