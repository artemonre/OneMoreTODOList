package com.artemonre.onemoretodolist.feature.backup.data

import com.artemonre.onemoretodolist.core.sync.ChecklistItemDto
import com.artemonre.onemoretodolist.core.sync.RecurrenceDto
import com.artemonre.onemoretodolist.core.sync.TodoDto
import com.artemonre.onemoretodolist.core.sync.TodoTagDto
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import com.artemonre.onemoretodolist.feature.todolist.domain.legacyCreatedAt
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// topSince is left out on purpose - see TodoDto.
fun TodoItem.toTodoDto(): TodoDto = TodoDto(
    id = id,
    text = text,
    status = status.name,
    sortOrder = sortOrder,
    creationDate = creationDate.toString(),
    lastEditDate = lastEditDate.toString(),
    createdAt = createdAt.toEpochMilliseconds(),
    completionDate = completionDate?.toString(),
    priorityOrder = priorityOrder,
    recurrence = recurrence?.let { RecurrenceDto(type = it.type.name, interval = it.interval, unit = it.unit.name) },
    recurrenceAnchorAt = recurrenceAnchorInstant?.toEpochMilliseconds(),
    dueDate = dueDate?.toString(),
    dueTime = dueTime?.toString(),
    dueTimeMode = dueTimeMode?.name,
    snoozedUntil = snoozedUntil?.toEpochMilliseconds(),
    checklist = checklist.map { ChecklistItemDto(id = it.id, text = it.text, isDone = it.isDone) },
    tags = tags.map { TodoTagDto(name = it.name, color = it.color.name) },
    updatedAt = updatedAt.toEpochMilliseconds(),
    deletedAt = deletedAt?.toEpochMilliseconds()
)

// Null when the todo can't be read - an unknown status/enum value or a malformed date, e.g. from a
// file written by a newer app version or edited by hand. Callers skip such a todo rather than
// failing the whole file. An unknown tag color or recurrence falls back gracefully instead, since
// the todo itself is still perfectly usable without them.
fun TodoDto.toTodoItemOrNull(): TodoItem? {
    return try {
        val creationDate = LocalDate.parse(creationDate)
        TodoItem(
            id = id,
            text = text,
            status = enumOrNull<TodoStatus>(status) ?: return null,
            sortOrder = sortOrder,
            creationDate = creationDate,
            lastEditDate = LocalDate.parse(lastEditDate),
            createdAt = createdAt?.let(Instant::fromEpochMilliseconds) ?: legacyCreatedAt(creationDate, sortOrder),
            completionDate = completionDate?.let(LocalDate::parse),
            priorityOrder = priorityOrder,
            recurrence = recurrence?.toRecurrenceOrNull(),
            recurrenceAnchorInstant = recurrenceAnchorAt?.let(Instant::fromEpochMilliseconds),
            dueDate = dueDate?.let(LocalDate::parse),
            dueTime = dueTime?.let(LocalTime::parse),
            dueTimeMode = dueTimeMode?.let { enumOrNull<DueTimeMode>(it) },
            snoozedUntil = snoozedUntil?.let(Instant::fromEpochMilliseconds),
            checklist = checklist.map { ChecklistItem(id = it.id, text = it.text, isDone = it.isDone) },
            tags = tags.map { TodoTag(name = it.name, color = enumOrNull<TagColor>(it.color) ?: TagColor.Blue) },
            updatedAt = Instant.fromEpochMilliseconds(updatedAt),
            deletedAt = deletedAt?.let(Instant::fromEpochMilliseconds)
        )
    } catch (e: IllegalArgumentException) {
        // kotlinx-datetime's parse failures (DateTimeFormatException) are IllegalArgumentExceptions.
        null
    }
}

private fun RecurrenceDto.toRecurrenceOrNull(): Recurrence? {
    val type = enumOrNull<RecurrenceType>(type) ?: return null
    val unit = enumOrNull<RecurrenceUnit>(unit) ?: return null
    if (interval < 1) return null
    return Recurrence(type = type, interval = interval, unit = unit)
}

private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? =
    enumValues<T>().firstOrNull { it.name == name }
