package com.artemonre.onemoretodolist.core.sync

import kotlinx.serialization.Serializable

// The wire/file shape of one todo - shared by the export file, the Google Drive backup and the
// future sync server, so it lives here in core (which the server can depend on) rather than next
// to the app's domain model. Only plain types: instants are epoch milliseconds, dates/times ISO
// strings, enums their names. Adding a field later must stay optional (with a default) so older
// files still read, and BackupJson ignores fields it doesn't know so newer files still read too.
//
// Not included on purpose: topSince - that's each device's own bookkeeping, not part of the todo.
@Serializable
data class TodoDto(
    val id: String,
    val text: String,
    val status: String,
    val sortOrder: Int,
    val creationDate: String,
    val lastEditDate: String,
    // Epoch millis. Null in files written before it existed - see legacyCreatedAt in the app.
    val createdAt: Long? = null,
    val completionDate: String? = null,
    val priorityOrder: Double? = null,
    val recurrence: RecurrenceDto? = null,
    val recurrenceAnchorAt: Long? = null,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val dueTimeMode: String? = null,
    val snoozedUntil: Long? = null,
    val checklist: List<ChecklistItemDto> = emptyList(),
    val tags: List<TodoTagDto> = emptyList(),
    // "Newest copy wins" key - see MergeTodos in the app.
    val updatedAt: Long,
    // Non-null means this is a deletion marker (tombstone), not a live todo.
    val deletedAt: Long? = null
)

@Serializable
data class RecurrenceDto(
    val type: String,
    val interval: Int,
    val unit: String
)

@Serializable
data class ChecklistItemDto(
    val id: String,
    val text: String,
    val isDone: Boolean = false
)

@Serializable
data class TodoTagDto(
    val name: String,
    val color: String
)
