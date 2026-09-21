package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class TodoDateConverters {
    @ColumnTypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.fromEpochDays(it) }

    @ColumnTypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDays()

    @ColumnTypeConverter
    fun toInstant(epochMillis: Long?): Instant? = epochMillis?.let { Instant.fromEpochMilliseconds(it) }

    @ColumnTypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilliseconds()

    // ISO-8601 string round-trip (e.g. "10:00") - simplest correct representation, and this
    // column is never queried/sorted on in SQL so there's no need for a numeric encoding.
    @ColumnTypeConverter
    fun toLocalTime(isoString: String?): LocalTime? = isoString?.let { LocalTime.parse(it) }

    @ColumnTypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()
}
