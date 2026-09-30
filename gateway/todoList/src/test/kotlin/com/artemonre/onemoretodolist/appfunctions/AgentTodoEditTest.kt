package com.artemonre.onemoretodolist.appfunctions

import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class AgentTodoEditTest {

    private val weekly = Recurrence(RecurrenceType.Every, 1, RecurrenceUnit.Week)
    private val checklist = listOf(ChecklistItem("a", "Step"))
    private val tags = listOf(TodoTag("Work", TagColor.Blue))
    private val todo = TodoItem(
        id = "1",
        text = "Water the plants",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        priorityOrder = 0.9,
        recurrence = weekly,
        dueDate = LocalDate(2026, 10, 3),
        dueTime = LocalTime(9, 0),
        dueTimeMode = DueTimeMode.Exact,
        checklist = checklist,
        tags = tags
    )

    @Test
    fun `passing nothing keeps every field as it is`() {
        assertEquals(
            TodoEditValues(
                text = todo.text,
                isPrioritized = true,
                recurrence = weekly,
                dueDate = todo.dueDate,
                dueTime = todo.dueTime,
                dueTimeMode = DueTimeMode.Exact,
                checklist = checklist,
                tags = tags
            ),
            todo.mergeAgentEdit()
        )
    }

    @Test
    fun `passed fields replace only themselves`() {
        val newChecklist = listOf(ChecklistItem("b", "Other"))

        val values = todo.mergeAgentEdit(text = "Water the garden", putToTop = false, checklist = newChecklist, tags = emptyList())

        assertEquals("Water the garden", values.text)
        assertEquals(false, values.isPrioritized)
        assertEquals(newChecklist, values.checklist)
        assertEquals(emptyList(), values.tags)
        assertEquals(weekly, values.recurrence)
        assertEquals(todo.dueTime, values.dueTime)
    }

    @Test
    fun `a new reminder keeps the current delivery mode unless one is given`() {
        val reminder = LocalDateTime(2026, 11, 1, 18, 30)

        val kept = todo.mergeAgentEdit(reminder = reminder)
        val overridden = todo.mergeAgentEdit(reminder = reminder, exactReminder = false)

        assertEquals(LocalDate(2026, 11, 1), kept.dueDate)
        assertEquals(LocalTime(18, 30), kept.dueTime)
        assertEquals(DueTimeMode.Exact, kept.dueTimeMode)
        assertEquals(DueTimeMode.Approximate, overridden.dueTimeMode)
    }

    @Test
    fun `exactReminder alone switches the mode of the existing reminder`() {
        val values = todo.mergeAgentEdit(exactReminder = false)

        assertEquals(todo.dueDate, values.dueDate)
        assertEquals(DueTimeMode.Approximate, values.dueTimeMode)
    }

    @Test
    fun `exactReminder alone does nothing without a reminder`() {
        val values = todo.copy(dueDate = null, dueTime = null, dueTimeMode = null).mergeAgentEdit(exactReminder = true)

        assertNull(values.dueTimeMode)
    }

    @Test
    fun `clearReminder wins over a reminder passed alongside it`() {
        val values = todo.mergeAgentEdit(reminder = LocalDateTime(2026, 11, 1, 18, 30), clearReminder = true)

        assertNull(values.dueDate)
        assertNull(values.dueTime)
        assertNull(values.dueTimeMode)
    }

    @Test
    fun `clearRecurrence wins over a recurrence passed alongside it`() {
        val values = todo.mergeAgentEdit(recurrence = weekly.copy(interval = 2), clearRecurrence = true)

        assertNull(values.recurrence)
        assertEquals(todo.dueDate, values.dueDate)
    }

    @Test
    fun `a new recurrence replaces the current one`() {
        val monthly = Recurrence(RecurrenceType.AfterCompletion, 1, RecurrenceUnit.Month)

        assertEquals(monthly, todo.mergeAgentEdit(recurrence = monthly).recurrence)
    }

    @Test
    fun `ending the recurrence drops a leftover time-of-day template`() {
        // An AfterCompletion todo between firings keeps only its time of day, no date.
        val template = todo.copy(
            recurrence = Recurrence(RecurrenceType.AfterCompletion, 1, RecurrenceUnit.Week),
            dueDate = null
        )

        val values = template.mergeAgentEdit(clearRecurrence = true)

        assertNull(values.dueTime)
        assertNull(values.dueTimeMode)
    }

    @Test
    fun `setting a reminder on a completed todo is rejected`() {
        val done = todo.copy(status = TodoStatus.Done)

        assertFailsWith<InvalidAgentInputException> {
            done.mergeAgentEdit(reminder = LocalDateTime(2026, 11, 1, 18, 30))
        }
    }

    @Test
    fun `other edits to a completed todo are allowed`() {
        val done = todo.copy(status = TodoStatus.Done)

        assertEquals("Renamed", done.mergeAgentEdit(text = "Renamed", clearReminder = true).text)
    }
}
