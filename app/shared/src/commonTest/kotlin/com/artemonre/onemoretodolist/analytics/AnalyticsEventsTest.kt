package com.artemonre.onemoretodolist.analytics

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.ONBOARDING_TODOS
import com.artemonre.onemoretodolist.feature.todolist.domain.SnoozeOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListFilter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class AnalyticsEventsTest {
    private val now = Instant.parse("2026-10-27T12:00:00Z")

    private fun todo(id: String = "1", topSince: Instant? = null) = TodoItem(
        id = id,
        text = "Buy milk",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 10, 1),
        lastEditDate = LocalDate(2026, 10, 1),
        topSince = topSince
    )

    @Test
    fun `enum values go out in snake_case`() {
        assertEquals("quick_sheet", TodoForm.QuickSheet.analyticsValue())
        assertEquals("app", AnalyticsSource.App.analyticsValue())
    }

    @Test
    fun `a todo that isn't the top one carries no highlight or onboarding properties`() {
        val event = AnalyticsEvents.todoCompleted(AnalyticsSource.App, archived = true, todo = todo(), now = now)

        assertEquals("todo_completed", event.name)
        assertEquals(mapOf("source" to "app", "archived" to true), event.properties.filterValues { it != null })
    }

    @Test
    fun `the top todo carries its highlight level and whole days on top`() {
        val event = AnalyticsEvents.todoDeleted(
            AnalyticsSource.App,
            TodoListFilter.Active,
            todo(topSince = now - 4.days - 2.hours),
            now = now
        )

        assertEquals(3, event.properties["attention_level"])
        assertEquals(4L, event.properties["days_on_top"])
    }

    @Test
    fun `a todo on top since today has days_on_top 0 and no highlight`() {
        val event = AnalyticsEvents.todoEdited(AnalyticsSource.App, todo(topSince = now - 3.hours), now = now)

        assertEquals(0, event.properties["attention_level"])
        assertEquals(0L, event.properties["days_on_top"])
    }

    @Test
    fun `onboarding todos are marked, and only they are`() {
        val onboarding = AnalyticsEvents.todoEdited(AnalyticsSource.App, todo(id = ONBOARDING_TODOS.first().id), now = now)
        val regular = AnalyticsEvents.todoEdited(AnalyticsSource.App, todo(), now = now)

        assertEquals(true, onboarding.properties["is_onboarding"])
        assertEquals(null, regular.properties["is_onboarding"])
    }

    @Test
    fun `a toggle outside the list is a completion for an active todo and a reopening for a done one`() {
        val active = todo()
        val done = todo().copy(status = TodoStatus.Done)

        assertEquals("todo_completed", AnalyticsEvents.todoToggled(AnalyticsSource.Widget, true, active, now).name)
        assertEquals("todo_reopened", AnalyticsEvents.todoToggled(AnalyticsSource.Agent, true, done, now).name)
        assertEquals("widget", AnalyticsEvents.todoToggled(AnalyticsSource.Widget, true, active, now).properties["source"])
    }

    @Test
    fun `background events say so, and events from someone using the app don't`() {
        assertEquals(true, AnalyticsEvents.reminderNotified(DueTimeMode.Exact).properties["background"])
        assertEquals(true, AnalyticsEvents.widgetPlaced(WidgetSize.Compact).properties["background"])
        assertEquals(null, AnalyticsEvents.widgetOpenedApp().properties["background"])
    }

    @Test
    fun `a Drive backup carries only the failure's enum value, and a daily run is a background event`() {
        val manualFailure = AnalyticsEvents.driveBackupRun(
            BackupTrigger.Manual,
            error = DataError.Remote.NO_INTERNET
        ).properties
        val autoSuccess = AnalyticsEvents.driveBackupRun(BackupTrigger.Auto, error = null).properties

        assertEquals(false, manualFailure["succeeded"])
        assertEquals("no_internet", manualFailure["error"])
        assertEquals(null, manualFailure["background"])
        assertEquals(true, autoSuccess["succeeded"])
        assertEquals(null, autoSuccess["error"])
        assertEquals(true, autoSuccess["background"])
    }

    @Test
    fun `snooze and reminder values come from their enums`() {
        assertEquals("one_hour", AnalyticsEvents.todoSnoozed(SnoozeOption.OneHour).properties["by"])
        assertEquals("approximate", AnalyticsEvents.reminderNotified(DueTimeMode.Approximate).properties["reminder_mode"])
        assertEquals("change_todo_done_status", AnalyticsEvents.agentFunctionCalled(AgentFunction.ChangeTodoDoneStatus, true).properties["function"])
    }

    @Test
    fun `the summary counts features over the given todos and never their content`() {
        val todos = listOf(
            todo().copy(dueDate = LocalDate(2026, 11, 1), tags = listOf(TodoTag("Home", TagColor.Red))),
            todo(id = "2").copy(checklist = listOf(ChecklistItem("a", "Eggs")), priorityOrder = 0.9),
            todo(id = "3")
        )

        val properties = AnalyticsEvents.todosSummary(todos).properties

        assertEquals(3, properties["active_count"])
        assertEquals(1, properties["with_reminder_count"])
        assertEquals(0, properties["with_repeat_count"])
        assertEquals(1, properties["with_tags_count"])
        assertEquals(1, properties["with_checklist_count"])
        assertEquals(1, properties["prioritized_count"])
        assertFalse(properties.values.any { it == "Buy milk" || it == "Home" || it == "Eggs" })
    }
}
