package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

enum class RecurrenceType {
    Every,
    AfterCompletion
}

enum class RecurrenceUnit {
    Day,
    Week,
    Month,
    Year
}

data class Recurrence(
    val type: RecurrenceType,
    val interval: Int,
    val unit: RecurrenceUnit
)

private fun RecurrenceUnit.toDuration(): Duration = when (this) {
    RecurrenceUnit.Day -> 1.days
    RecurrenceUnit.Week -> 7.days
    RecurrenceUnit.Month -> 30.days
    RecurrenceUnit.Year -> 365.days
}

fun Recurrence.duration(): Duration = unit.toDuration() * interval
