package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.ColumnTypeConverter
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class TodoDateConverters {
    @ColumnTypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.fromEpochDays(it) }

    @ColumnTypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDays()

    @ColumnTypeConverter
    fun toInstant(epochMillis: Long?): Instant? = epochMillis?.let { Instant.fromEpochMilliseconds(it) }

    @ColumnTypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilliseconds()
}
