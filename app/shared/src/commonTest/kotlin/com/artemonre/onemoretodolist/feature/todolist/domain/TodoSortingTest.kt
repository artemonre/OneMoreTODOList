package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
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

    private fun todoItem(
        id: String,
        creationDate: LocalDate,
        sortOrder: Int
    ) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = sortOrder,
        creationDate = creationDate,
        lastEditDate = creationDate
    )
}
