package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Entity(tableName = "todo_items")
data class TodoEntity(
    @PrimaryKey val id: String,
    val text: String,
    val status: TodoStatus,
    val sortOrder: Int,
    val creationDate: LocalDate,
    val lastEditDate: LocalDate,
    val completionDate: LocalDate?,
    val priorityOrder: Double?,
    // type/interval/unit all null together means "does not repeat" - see TodoMappers.
    // recurrenceAnchorInstant is recurrence bookkeeping only, see TodoItem.
    val recurrenceType: RecurrenceType? = null,
    val recurrenceInterval: Int? = null,
    val recurrenceUnit: RecurrenceUnit? = null,
    val recurrenceAnchorInstant: Instant? = null,
    val topSince: Instant? = null,
    // dueDate/dueTime/dueTimeMode null together means "no due time set" - wall-clock local values,
    // not a resolved instant - see TodoMappers/TodoItem.
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val dueTimeMode: DueTimeMode? = null,
    val snoozedUntil: Instant? = null,
    // JSON arrays (see TodoMappers) rather than separate tables - both are small, always loaded
    // together with their todo, and never queried on their own in SQL.
    @ColumnInfo(defaultValue = "[]") val checklist: String = "[]",
    @ColumnInfo(defaultValue = "[]") val tags: String = "[]",
    // Epoch millis. The 0 default only exists so the 9 -> 10 AutoMigration can add a NOT NULL
    // column - BackfillUpdatedAt then replaces it on every existing row.
    @ColumnInfo(defaultValue = "0") val updatedAt: Instant = Instant.fromEpochMilliseconds(0),
    val deletedAt: Instant? = null
)
