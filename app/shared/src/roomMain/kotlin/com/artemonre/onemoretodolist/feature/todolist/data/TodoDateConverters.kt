package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDate

class TodoDateConverters {
    @ColumnTypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.fromEpochDays(it) }

    @ColumnTypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDays()
}
