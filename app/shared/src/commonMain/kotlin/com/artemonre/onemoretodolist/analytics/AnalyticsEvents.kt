package com.artemonre.onemoretodolist.analytics

import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.ONBOARDING_TODOS
import com.artemonre.onemoretodolist.feature.todolist.domain.SnoozeOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.topTodoAttention
import com.artemonre.onemoretodolist.feature.todolist.presentation.EmptyStateTip
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListFilter
import kotlin.time.Clock
import kotlin.time.Instant

// Every event the app sends, named and shaped as ANALYTICS.md describes it. Never put todo text,
// tag names, checklist text or file names in here - counts, flags and enum values only.

enum class AnalyticsSource { App, Widget, Notification, Agent }

enum class TodoForm { QuickSheet, FullForm, QuickAdd, Scanned, Agent }

enum class ShareMethod { Native, Copied }

enum class UndoneAction { Completed, Deleted }

enum class PhotoInput { Camera, Gallery }

enum class ScannedTextChoice { Separate, Single }

// Which widget-picker entry - all three show the same TodoWidget, at different default sizes.
enum class WidgetSize { Full, Compact, Row }

enum class AgentFunction { GetTodoList, AddTodo, EditTodo, ChangeTodoDoneStatus, DeleteTodo }

enum class AppPermission { Notifications, ExactAlarm }

enum class BackupTrigger { Manual, Auto }

enum class UpdateEntryPoint { Settings, MandatoryScreen }

// Events nobody's use of the app caused (an alarm, a worker, the launcher) - dashboards filter them
// out of active-user counts with this one property.
private val BACKGROUND = "background" to true

// Enum values go out as their Kotlin names in snake_case ("QuickSheet" -> "quick_sheet"), so a new
// enum value needs no mapping of its own.
internal fun Enum<*>.analyticsValue(): String =
    name.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), "_").lowercase()

private val onboardingTodoIds = ONBOARDING_TODOS.map { it.id }.toSet()

// What every event about one todo carries: the top-of-list highlight only for the top todo (topSince
// is null for every other one), is_onboarding only for the seeded example todos.
// attention_level is TopTodoAttention's position - 0 for None, then one per step in order.
private fun TodoItem.analyticsProperties(now: Instant): Map<String, Any?> = mapOf(
    "attention_level" to topSince?.let { topTodoAttention(it, now).ordinal },
    "days_on_top" to topSince?.let { (now - it).inWholeDays },
    "is_onboarding" to (id in onboardingTodoIds).takeIf { it }
)

object AnalyticsEvents {
    // 1. Todos

    fun todoCreated(source: AnalyticsSource, form: TodoForm) = AnalyticsEvent(
        "todo_created",
        mapOf("source" to source.analyticsValue(), "form" to form.analyticsValue())
    )

    fun todoEdited(source: AnalyticsSource, todo: TodoItem, now: Instant = Clock.System.now()) = AnalyticsEvent(
        "todo_edited",
        mapOf("source" to source.analyticsValue()) + todo.analyticsProperties(now)
    )

    fun todoCompleted(
        source: AnalyticsSource,
        archived: Boolean,
        todo: TodoItem,
        now: Instant = Clock.System.now()
    ) = AnalyticsEvent(
        "todo_completed",
        mapOf("source" to source.analyticsValue(), "archived" to archived) + todo.analyticsProperties(now)
    )

    fun todoReopened(source: AnalyticsSource, todo: TodoItem, now: Instant = Clock.System.now()) = AnalyticsEvent(
        "todo_reopened",
        mapOf("source" to source.analyticsValue()) + todo.analyticsProperties(now)
    )

    fun todoDeleted(
        source: AnalyticsSource,
        fromFilter: TodoListFilter,
        todo: TodoItem,
        now: Instant = Clock.System.now()
    ) = AnalyticsEvent(
        "todo_deleted",
        mapOf("source" to source.analyticsValue(), "from_filter" to fromFilter.analyticsValue()) +
            todo.analyticsProperties(now)
    )

    fun todoUndone(undid: UndoneAction) = AnalyticsEvent("todo_undone", mapOf("undid" to undid.analyticsValue()))

    fun todosReordered(switchedToManual: Boolean) =
        AnalyticsEvent("todos_reordered", mapOf("switched_to_manual" to switchedToManual))

    fun todoShared(method: ShareMethod) = AnalyticsEvent("todo_shared", mapOf("method" to method.analyticsValue()))

    fun todoDetailsOpened() = AnalyticsEvent("todo_details_opened")

    fun checklistItemToggled() = AnalyticsEvent("checklist_item_toggled")

    // Completing and reopening are the same toggle everywhere outside the list - which one it was
    // follows from the todo as it was before the toggle.
    fun todoToggled(
        source: AnalyticsSource,
        archived: Boolean,
        todoBefore: TodoItem,
        now: Instant = Clock.System.now()
    ) = if (todoBefore.status == TodoStatus.Active) {
        todoCompleted(source, archived, todoBefore, now)
    } else {
        todoReopened(source, todoBefore, now)
    }

    fun voiceInputUsed() = AnalyticsEvent("voice_input_used")

    // Over active todos only - what's in use right now, not what's been finished.
    fun todosSummary(activeTodos: List<TodoItem>) = AnalyticsEvent(
        "todos_summary",
        mapOf(
            "active_count" to activeTodos.size,
            "with_reminder_count" to activeTodos.count { it.dueDate != null },
            "with_repeat_count" to activeTodos.count { it.recurrence != null },
            "with_tags_count" to activeTodos.count { it.tags.isNotEmpty() },
            "with_checklist_count" to activeTodos.count { it.checklist.isNotEmpty() },
            "prioritized_count" to activeTodos.count { it.priorityOrder != null }
        )
    )

    // 2. List

    fun listSorted(sortOption: TodoSortOption) =
        AnalyticsEvent("list_sorted", mapOf("sort_option" to sortOption.analyticsValue()))

    fun listFiltered(filter: TodoListFilter) =
        AnalyticsEvent("list_filtered", mapOf("filter" to filter.analyticsValue()))

    fun addFormExpanded() = AnalyticsEvent("add_form_expanded")

    fun reorderUnavailableShown() = AnalyticsEvent("reorder_unavailable_shown")

    // 3. Empty states

    fun emptyStateTipShown(tip: EmptyStateTip) =
        AnalyticsEvent("empty_state_tip_shown", mapOf("tip" to tip.analyticsValue()))

    fun widgetPinRequested(shown: Boolean) = AnalyticsEvent("widget_pin_requested", mapOf("shown" to shown))

    fun keepCompletedEnabled() = AnalyticsEvent("keep_completed_enabled")

    // 4. Photo capture

    fun photoCaptureOpened() = AnalyticsEvent("photo_capture_opened")

    // script is the platform recognizer's own enum - only Android has one.
    fun photoTextRecognized(input: PhotoInput, script: Enum<*>, textFound: Boolean) = AnalyticsEvent(
        "photo_text_recognized",
        mapOf("input" to input.analyticsValue(), "script" to script.analyticsValue(), "text_found" to textFound)
    )

    fun scannedTextConfirmed(choice: ScannedTextChoice) =
        AnalyticsEvent("scanned_text_confirmed", mapOf("as" to choice.analyticsValue()))

    fun scannedTextRetaken() = AnalyticsEvent("scanned_text_retaken")

    // 5. Widget, quick add and notifications

    fun widgetPlaced(size: WidgetSize) =
        AnalyticsEvent("widget_placed", mapOf("size" to size.analyticsValue(), BACKGROUND))

    fun widgetRemoved(size: WidgetSize) =
        AnalyticsEvent("widget_removed", mapOf("size" to size.analyticsValue(), BACKGROUND))

    fun widgetOpenedApp() = AnalyticsEvent("widget_opened_app")

    fun quickAddOpened() = AnalyticsEvent("quick_add_opened")

    fun reminderNotified(mode: DueTimeMode?) =
        AnalyticsEvent("reminder_notified", mapOf("reminder_mode" to mode?.analyticsValue(), BACKGROUND))

    fun notificationOpened() = AnalyticsEvent("notification_opened")

    fun todoSnoozed(option: SnoozeOption) = AnalyticsEvent("todo_snoozed", mapOf("by" to option.analyticsValue()))

    // 6. AI agents

    fun agentFunctionCalled(function: AgentFunction, succeeded: Boolean) = AnalyticsEvent(
        "agent_function_called",
        mapOf("function" to function.analyticsValue(), "succeeded" to succeeded)
    )

    // 7. Settings - the shape setting_changed had before this file existed, so existing dashboards
    // keep working.

    fun settingChanged(screen: String, section: String, option: String) = AnalyticsEvent(
        "setting_changed",
        mapOf("screen" to screen, "section" to section, "option" to option)
    )

    fun permissionResult(permission: AppPermission, granted: Boolean) = AnalyticsEvent(
        "permission_result",
        mapOf("permission" to permission.analyticsValue(), "granted" to granted)
    )

    // 8. Backup

    fun backupExported(saved: Boolean) = AnalyticsEvent("backup_exported", mapOf("saved" to saved))

    fun backupImported(mode: ImportMode, succeeded: Boolean) = AnalyticsEvent(
        "backup_imported",
        mapOf("import_mode" to mode.analyticsValue(), "succeeded" to succeeded)
    )

    // error is the failure's enum value only (e.g. no_internet) - never a message. A daily run is a
    // background event, a tap on Back up now is not.
    fun driveBackupRun(trigger: BackupTrigger, error: Enum<*>?) = AnalyticsEvent(
        "drive_backup_run",
        mapOf(
            "trigger" to trigger.analyticsValue(),
            "succeeded" to (error == null),
            "error" to error?.analyticsValue(),
            "background" to (trigger == BackupTrigger.Auto).takeIf { it }
        )
    )

    fun driveRestored(mode: ImportMode, succeeded: Boolean) = AnalyticsEvent(
        "drive_restored",
        mapOf("import_mode" to mode.analyticsValue(), "succeeded" to succeeded)
    )

    fun driveConnected(succeeded: Boolean) = AnalyticsEvent("drive_connected", mapOf("succeeded" to succeeded))

    fun driveAutoBackupChanged(enabled: Boolean) =
        AnalyticsEvent("drive_auto_backup_changed", mapOf("enabled" to enabled))

    fun driveDisconnected() = AnalyticsEvent("drive_disconnected")

    fun fileAutoBackupChanged(enabled: Boolean) =
        AnalyticsEvent("file_auto_backup_changed", mapOf("enabled" to enabled))

    // 9. App updates

    fun updateCheckFinished(available: Boolean, mandatory: Boolean, durationMs: Long) = AnalyticsEvent(
        "update_check_finished",
        mapOf("available" to available, "mandatory" to mandatory, "duration_ms" to durationMs)
    )

    // Only the Immediate flow exists for now - see AppUpdate.android.kt.
    fun updateStarted(from: UpdateEntryPoint) = AnalyticsEvent(
        "update_started",
        mapOf("type" to "immediate", "from" to from.analyticsValue())
    )

    // 10. Navigation

    // tab: the tab label's resource key without its "tab_" prefix - stable across languages.
    fun tabSelected(tab: String) = AnalyticsEvent("tab_selected", mapOf("tab" to tab))
}
