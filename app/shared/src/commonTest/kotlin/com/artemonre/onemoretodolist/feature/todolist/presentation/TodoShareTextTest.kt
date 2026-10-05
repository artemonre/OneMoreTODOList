package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class TodoShareTextTest {

    private fun item(
        checklist: List<ChecklistItem> = emptyList(),
        tags: List<TodoTag> = emptyList()
    ) = TodoItemUi(
        id = "1",
        text = "Buy groceries",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 10, 1),
        checklist = checklist,
        tags = tags
    )

    @Test
    fun `plain todo shares just its text`() {
        assertEquals("Buy groceries", item().toShareText(reminder = null))
    }

    @Test
    fun `checklist, reminder and tags follow the text in order`() {
        val shared = item(
            checklist = listOf(ChecklistItem("a", "Milk", isDone = true), ChecklistItem("b", "Bread")),
            tags = listOf(TodoTag("home", TagColor.Green), TodoTag("weekly", TagColor.Blue))
        ).toShareText(reminder = "12 Oct 2026, 18:00")

        assertEquals("Buy groceries\n☑ Milk\n☐ Bread\n⏰ 12 Oct 2026, 18:00\n#home #weekly", shared)
    }

    @Test
    fun `hashtags join words with underscores`() {
        assertEquals("#work_stuff", TodoTag("  work  stuff ", TagColor.Red).toHashtag())
        assertEquals("#Кухня_2", TodoTag("Кухня-2", TagColor.Red).toHashtag())
    }

    @Test
    fun `tag with no letters or digits is dropped`() {
        assertNull(TodoTag("!!", TagColor.Red).toHashtag())
        assertEquals("Buy groceries", item(tags = listOf(TodoTag("!!", TagColor.Red))).toShareText(reminder = null))
    }
}
