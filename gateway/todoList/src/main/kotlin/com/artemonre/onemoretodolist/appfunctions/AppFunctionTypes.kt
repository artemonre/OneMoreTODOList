package com.artemonre.onemoretodolist.appfunctions

import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionStringValueConstraint
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

// The shapes agents read and write through BaseTodoAppFunctionService, plus their mapping to and
// from the domain. Enum-like fields are plain constrained strings - that's what AppFunctions
// schemas express - validated here on the way in.

// Invalid agent input, thrown by the plain mapping/merge functions here and in AgentTodoEdit. The
// service turns it into AppFunctionInvalidArgumentException at its boundary (see agentInput) -
// that type needs the Android framework to construct, which would keep these functions from
// running in plain JVM unit tests.
internal class InvalidAgentInputException(message: String) : RuntimeException(message)

/** How a todo repeats. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class TodoRecurrence(
    /**
     * "EVERY" repeats on a fixed schedule regardless of completion; "AFTER_COMPLETION" repeats the
     * given interval after the user completes it.
     */
    @AppFunctionStringValueConstraint(enumValues = ["EVERY", "AFTER_COMPLETION"])
    val type: String,
    /** How many units between repeats, at least 1. */
    val interval: Int,
    /** The unit of the interval. */
    @AppFunctionStringValueConstraint(enumValues = ["DAY", "WEEK", "MONTH", "YEAR"])
    val unit: String
)

/** One line of a todo's checklist. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class TodoChecklistItem(
    /** The item's ID. When sending a checklist, pass an existing ID to keep that item, or null for a new item. */
    val id: String?,
    /** The item's text. */
    val text: String,
    /** Whether the item is ticked off. */
    val isDone: Boolean
)

/** A tag on a todo. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class TodoTagItem(
    /** The tag's name. The same name always means the same tag. */
    val name: String,
    /**
     * The tag's color. When sending, null reuses the color the tag already has on other todos, or
     * picks one for a brand-new tag.
     */
    @AppFunctionStringValueConstraint(enumValues = ["RED", "ORANGE", "YELLOW", "GREEN", "TEAL", "BLUE", "PURPLE", "PINK"])
    val color: String?
)

/** A todo as seen by an agent. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class TodoSummary(
    /** Unique ID of the todo - pass it to editTodo, changeTodoDoneStatus or deleteTodo. */
    val id: String,
    /** The todo's text. */
    val text: String,
    /** Either "ACTIVE" (still to do) or "DONE" (completed). */
    val status: String,
    /** Whether the user pinned this todo to the top of their list. */
    val isPrioritized: Boolean,
    /** Local date and time of the next reminder in ISO-8601 (e.g. "2026-10-03T09:30"), or null if none is set. */
    val reminderDateTime: String?,
    /** Whether the reminder fires at the exact time (true) or within a 15-minute window around it (false). */
    val reminderExact: Boolean,
    /** How the todo repeats, or null if it doesn't. A completed repeating todo becomes active again on its own. */
    val recurrence: TodoRecurrence?,
    /** The todo's checklist, in order (empty if it has none). */
    val checklist: List<TodoChecklistItem>,
    /** The todo's tags, in the order the user added them. */
    val tags: List<TodoTagItem>,
    /** Local date the todo was created, in ISO-8601 (e.g. "2026-09-30"). */
    val creationDate: String,
    /** Local date the todo was completed in ISO-8601, or null while it's active. */
    val completionDate: String?
)

internal fun TodoItem.toTodoSummary(): TodoSummary = TodoSummary(
    id = id,
    text = text,
    status = if (status == TodoStatus.Done) "DONE" else "ACTIVE",
    isPrioritized = priorityOrder != null,
    reminderDateTime = dueDate?.let { date -> dueTime?.let { time -> LocalDateTime(date, time).toString() } },
    reminderExact = dueTimeMode == DueTimeMode.Exact,
    recurrence = recurrence?.let { TodoRecurrence(it.type.toApiName(), it.interval, it.unit.name.uppercase()) },
    checklist = checklist.map { TodoChecklistItem(it.id, it.text, it.isDone) },
    tags = tags.map { TodoTagItem(it.name, it.color.name.uppercase()) },
    creationDate = creationDate.toString(),
    completionDate = completionDate?.toString()
)

private fun RecurrenceType.toApiName(): String = when (this) {
    RecurrenceType.Every -> "EVERY"
    RecurrenceType.AfterCompletion -> "AFTER_COMPLETION"
}

internal fun TodoRecurrence.toRecurrence(): Recurrence {
    val type = when (type) {
        "EVERY" -> RecurrenceType.Every
        "AFTER_COMPLETION" -> RecurrenceType.AfterCompletion
        else -> throw InvalidAgentInputException("Invalid recurrence type: $type. Must be EVERY or AFTER_COMPLETION.")
    }
    val unit = RecurrenceUnit.entries.firstOrNull { it.name.equals(unit, ignoreCase = true) }
        ?: throw InvalidAgentInputException("Invalid recurrence unit: $unit. Must be DAY, WEEK, MONTH, or YEAR.")
    if (interval < 1) {
        throw InvalidAgentInputException("Recurrence interval must be at least 1, got $interval")
    }
    return Recurrence(type = type, interval = interval, unit = unit)
}

// Items with a known ID keep it (so the agent can re-send a list it read and edit a line in place);
// new ones get a fresh ID. Blank lines are dropped, same as the app's form.
@OptIn(ExperimentalUuidApi::class)
internal fun List<TodoChecklistItem>.toChecklist(): List<ChecklistItem> =
    filter { it.text.isNotBlank() }
        .map { ChecklistItem(id = it.id ?: Uuid.random().toString(), text = it.text.trim(), isDone = it.isDone) }

// A name already used on another todo keeps its color unless one is given explicitly - same
// "one name, one color" rule the app's tag picker follows (see knownTags).
internal fun List<TodoTagItem>.toTags(knownTags: List<TodoTag>): List<TodoTag> =
    filter { it.name.isNotBlank() }
        .distinctBy { it.name.trim().lowercase() }
        .map { item ->
            val name = item.name.trim()
            val known = knownTags.firstOrNull { it.name.equals(name, ignoreCase = true) }
            val color = item.color?.let { requested ->
                TagColor.entries.firstOrNull { it.name.equals(requested, ignoreCase = true) }
                    ?: throw InvalidAgentInputException("Invalid tag color: $requested")
            } ?: known?.color ?: TagColor.entries.random()
            TodoTag(name = known?.name ?: name, color = color)
        }

// Wall-clock local time, same as the app's own due times - see TodoItem.dueInstant.
internal fun parseFutureReminder(
    value: String,
    now: Instant = Clock.System.now(),
    zone: TimeZone = TimeZone.currentSystemDefault()
): LocalDateTime {
    val dateTime = try {
        LocalDateTime.parse(value)
    } catch (e: IllegalArgumentException) {
        throw InvalidAgentInputException("Invalid reminderDateTime: $value. Use ISO-8601, e.g. 2026-10-03T09:30.")
    }
    if (dateTime.toInstant(zone) <= now) {
        throw InvalidAgentInputException("reminderDateTime $value is in the past")
    }
    return dateTime
}
