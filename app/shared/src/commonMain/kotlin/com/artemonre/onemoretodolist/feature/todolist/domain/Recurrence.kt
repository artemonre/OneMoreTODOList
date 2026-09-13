package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

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

// TESTING OVERRIDE - revert to real calendar durations (1.days, 7.days, 30.days, 365.days) once
// manual testing of recurrence is done. Compressed to minutes (same 1-unit-to-1-minute ratio
// throughout) so "every"/"after completion" cycles are observable in a normal testing session
// instead of over real days/weeks/months.
private fun RecurrenceUnit.toTestingDuration(): Duration = when (this) {
    RecurrenceUnit.Day -> 1.minutes
    RecurrenceUnit.Week -> 7.minutes
    RecurrenceUnit.Month -> 30.minutes
    RecurrenceUnit.Year -> 365.minutes
}

fun Recurrence.duration(): Duration = unit.toTestingDuration() * interval
