package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ComposeUiTest
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.form_discard
import org.jetbrains.compose.resources.getString

// TodoFormBody's side of the unsaved add draft: it reopens exactly as it was kept, and reports
// every change back to its host.
@OptIn(ExperimentalTestApi::class)
class TodoFormDraftTest {

    // Every field the full-screen form shows. Approximate, since the desktop JVM has no exact-alarm
    // permission to start exact mode from.
    private val fullDraft = TodoDraft(
        text = "Buy milk",
        isPrioritized = true,
        recurrence = Recurrence(type = RecurrenceType.Every, interval = 2, unit = RecurrenceUnit.Week),
        dueDate = LocalDate(2099, 1, 1),
        dueTime = LocalTime(9, 30),
        dueTimeMode = DueTimeMode.Approximate,
        checklist = listOf(ChecklistItem(id = "c1", text = "Oat", isDone = false)),
        tags = listOf(TodoTag(name = "home", color = TagColor.Green))
    )

    private fun ComposeUiTest.showForm(
        initialDraft: TodoDraft?,
        onDraftChange: (TodoDraft) -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        setContent {
            AppTheme(themeConfig = ThemeConfig()) {
                TodoFormBody(
                    editingItem = null,
                    onConfirm = {},
                    onDismiss = onDismiss,
                    // Taller than the test window with every section shown - same as the hosts do.
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    showDueTime = true,
                    showRecurrence = true,
                    showTags = true,
                    showChecklist = true,
                    initialDraft = initialDraft,
                    onDraftChange = onDraftChange
                )
            }
        }
    }

    @Test
    fun `a kept draft reopens with every field as it was`() = runComposeUiTest {
        val drafts = mutableListOf<TodoDraft>()
        showForm(initialDraft = fullDraft, onDraftChange = { drafts += it })
        waitForIdle()

        assertEquals(fullDraft, drafts.last())
    }

    @Test
    fun `typing reaches the host as the new draft`() = runComposeUiTest {
        val drafts = mutableListOf<TodoDraft>()
        showForm(initialDraft = null, onDraftChange = { drafts += it })

        onAllNodes(hasSetTextAction()).onFirst().performTextInput("Call mom")
        waitForIdle()

        assertEquals(TodoDraft(text = "Call mom", isPrioritized = false), drafts.last())
    }

    @Test
    fun `Discard reaches the host through onDismiss`() = runComposeUiTest {
        var discarded = 0
        showForm(initialDraft = fullDraft, onDismiss = { discarded++ })

        onNode(hasText(runBlocking { getString(Res.string.form_discard) })).performScrollTo().performClick()

        assertEquals(1, discarded)
    }
}
