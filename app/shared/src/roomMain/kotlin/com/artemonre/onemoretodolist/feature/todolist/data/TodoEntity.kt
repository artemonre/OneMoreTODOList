package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

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
    val topSince: Instant? = null
)
