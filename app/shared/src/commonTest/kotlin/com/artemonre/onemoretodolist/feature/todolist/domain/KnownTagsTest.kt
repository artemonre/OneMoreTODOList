package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class KnownTagsTest {

    @Test
    fun `known tags are distinct by name ignoring case, keep the first color, and sort by name`() {
        val todos = listOf(
            todoItem("1", TodoTag("work", TagColor.Blue), TodoTag("Home", TagColor.Orange)),
            todoItem("2", TodoTag("Work", TagColor.Red), TodoTag("Errands", TagColor.Green))
        )

        assertEquals(
            listOf(TodoTag("Errands", TagColor.Green), TodoTag("Home", TagColor.Orange), TodoTag("work", TagColor.Blue)),
            todos.knownTags()
        )
    }

    private fun todoItem(id: String, vararg tags: TodoTag) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        tags = tags.toList()
    )
}
