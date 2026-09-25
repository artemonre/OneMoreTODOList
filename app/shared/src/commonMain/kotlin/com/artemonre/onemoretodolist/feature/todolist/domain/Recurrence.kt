package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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

// Advances a date by one recurrence interval via Instant math - callers keep the original
// LocalTime fixed rather than deriving it from the shifted instant, which is what pins a chosen
// time of day (e.g. 10am) across every future occurrence instead of letting it drift.
fun Recurrence.nextDate(from: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    (from + duration()).toLocalDateTime(zone).date
