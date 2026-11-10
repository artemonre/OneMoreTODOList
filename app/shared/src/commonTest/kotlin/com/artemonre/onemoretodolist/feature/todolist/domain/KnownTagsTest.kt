package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class KnownTagsTest {

    @Test
    fun `known tags are distinct by name ignoring case, keep the first color, and sort by name`() {
        val todos = listOf(
            todoItem("1", TodoTag("work", TagColor.Color6), TodoTag("Home", TagColor.Color2)),
            todoItem("2", TodoTag("Work", TagColor.Color1), TodoTag("Errands", TagColor.Color4))
        )

        assertEquals(
            listOf(TodoTag("Errands", TagColor.Color4), TodoTag("Home", TagColor.Color2), TodoTag("work", TagColor.Color6)),
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
