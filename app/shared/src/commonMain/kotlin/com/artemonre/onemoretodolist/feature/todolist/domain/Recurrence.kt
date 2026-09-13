package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

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

fun RecurrenceUnit.toDateTimeUnit(): DateTimeUnit.DateBased = when (this) {
    RecurrenceUnit.Day -> DateTimeUnit.DAY
    RecurrenceUnit.Week -> DateTimeUnit.WEEK
    RecurrenceUnit.Month -> DateTimeUnit.MONTH
    RecurrenceUnit.Year -> DateTimeUnit.YEAR
}

fun LocalDate.plus(recurrence: Recurrence): LocalDate = plus(recurrence.interval, recurrence.unit.toDateTimeUnit())
