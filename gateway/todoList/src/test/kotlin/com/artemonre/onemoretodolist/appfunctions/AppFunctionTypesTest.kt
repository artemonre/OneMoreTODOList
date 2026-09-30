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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

class AppFunctionTypesTest {

    // ---------------- TodoItem -> TodoSummary

    @Test
    fun `an active todo maps every field`() {
        val todo = TodoItem(
            id = "1",
            text = "Pack",
            status = TodoStatus.Active,
            sortOrder = 0,
            creationDate = LocalDate(2026, 9, 30),
            lastEditDate = LocalDate(2026, 9, 30),
            priorityOrder = 0.9,
            recurrence = Recurrence(RecurrenceType.AfterCompletion, 2, RecurrenceUnit.Week),
            dueDate = LocalDate(2026, 10, 3),
            dueTime = LocalTime(9, 30),
            dueTimeMode = DueTimeMode.Exact,
            checklist = listOf(ChecklistItem("a", "Passport", isDone = true)),
            tags = listOf(TodoTag("Travel", TagColor.Teal))
        )

        assertEquals(
            TodoSummary(
                id = "1",
                text = "Pack",
                status = "ACTIVE",
                isPrioritized = true,
                reminderDateTime = "2026-10-03T09:30",
                reminderExact = true,
                recurrence = TodoRecurrence("AFTER_COMPLETION", 2, "WEEK"),
                checklist = listOf(TodoChecklistItem("a", "Passport", isDone = true)),
                tags = listOf(TodoTagItem("Travel", "TEAL")),
                creationDate = "2026-09-30",
                completionDate = null
            ),
            todo.toTodoSummary()
        )
    }

    @Test
    fun `a done todo with no reminder or recurrence maps to nulls`() {
        val todo = TodoItem(
            id = "1",
            text = "Pack",
            status = TodoStatus.Done,
            sortOrder = 0,
            creationDate = LocalDate(2026, 9, 30),
            lastEditDate = LocalDate(2026, 9, 30),
            completionDate = LocalDate(2026, 10, 1),
            // A time-of-day template without a date isn't a reminder yet.
            dueTime = LocalTime(9, 30)
        )

        val summary = todo.toTodoSummary()

        assertEquals("DONE", summary.status)
        assertEquals(false, summary.isPrioritized)
        assertNull(summary.reminderDateTime)
        assertEquals(false, summary.reminderExact)
        assertNull(summary.recurrence)
        assertEquals("2026-10-01", summary.completionDate)
    }

    // ---------------- TodoRecurrence -> Recurrence

    @Test
    fun `a valid recurrence converts, unit case-insensitively`() {
        assertEquals(
            Recurrence(RecurrenceType.Every, 3, RecurrenceUnit.Day),
            TodoRecurrence("EVERY", 3, "day").toRecurrence()
        )
    }

    @Test
    fun `an invalid recurrence is rejected`() {
        assertFailsWith<InvalidAgentInputException> { TodoRecurrence("SOMETIMES", 1, "DAY").toRecurrence() }
        assertFailsWith<InvalidAgentInputException> { TodoRecurrence("EVERY", 1, "FORTNIGHT").toRecurrence() }
        assertFailsWith<InvalidAgentInputException> { TodoRecurrence("EVERY", 0, "DAY").toRecurrence() }
    }

    // ---------------- checklist

    @Test
    fun `checklist keeps given ids, creates new ones, trims text and drops blank lines`() {
        val items = listOf(
            TodoChecklistItem(id = "keep", text = " Passport ", isDone = true),
            TodoChecklistItem(id = null, text = "Tickets", isDone = false),
            TodoChecklistItem(id = null, text = "   ", isDone = false)
        ).toChecklist()

        assertEquals(2, items.size)
        assertEquals(ChecklistItem("keep", "Passport", isDone = true), items[0])
        assertEquals("Tickets", items[1].text)
        assertNotNull(items[1].id.takeIf { it.isNotBlank() })
    }

    // ---------------- tags

    private val knownTags = listOf(TodoTag("Work", TagColor.Blue))

    @Test
    fun `a known tag name keeps its color and original spelling`() {
        assertEquals(listOf(TodoTag("Work", TagColor.Blue)), listOf(TodoTagItem(" work ", null)).toTags(knownTags))
    }

    @Test
    fun `an explicit color wins, case-insensitively`() {
        assertEquals(listOf(TodoTag("Work", TagColor.Red)), listOf(TodoTagItem("Work", "red")).toTags(knownTags))
    }

    @Test
    fun `a new tag without a color still gets one`() {
        val tag = listOf(TodoTagItem("Home", null)).toTags(knownTags).single()

        assertEquals("Home", tag.name)
        assertNotNull(tag.color)
    }

    @Test
    fun `blank and duplicate tag names are dropped`() {
        val tags = listOf(TodoTagItem("Home", "GREEN"), TodoTagItem("home", "RED"), TodoTagItem(" ", null)).toTags(knownTags)

        assertEquals(listOf(TodoTag("Home", TagColor.Green)), tags)
    }

    @Test
    fun `an unknown tag color is rejected`() {
        assertFailsWith<InvalidAgentInputException> { listOf(TodoTagItem("Home", "BEIGE")).toTags(knownTags) }
    }

    // ---------------- reminder parsing

    private val now = Instant.parse("2026-09-30T10:00:00Z")

    @Test
    fun `a future ISO local date-time parses`() {
        assertEquals(
            LocalDateTime(2026, 10, 3, 9, 30),
            parseFutureReminder("2026-10-03T09:30", now, TimeZone.UTC)
        )
    }

    @Test
    fun `a past or malformed reminder is rejected`() {
        assertFailsWith<InvalidAgentInputException> { parseFutureReminder("2026-09-30T09:59", now, TimeZone.UTC) }
        assertFailsWith<InvalidAgentInputException> { parseFutureReminder("tomorrow at 9", now, TimeZone.UTC) }
    }

    @Test
    fun `the reminder is checked against the given timezone`() {
        // 11:30 in Berlin (UTC+2 on this date) is 09:30 UTC - already past at 10:00 UTC.
        assertFailsWith<InvalidAgentInputException> {
            parseFutureReminder("2026-09-30T11:30", now, TimeZone.of("Europe/Berlin"))
        }
    }
}
