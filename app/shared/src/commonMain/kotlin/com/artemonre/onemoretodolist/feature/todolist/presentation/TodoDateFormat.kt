package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.month_names_short
import org.jetbrains.compose.resources.stringArrayResource

// "24 Aug 2026" with the month name in the current language - kotlinx-datetime only ships English
// month names, so they come from a string-array resource instead.
@Composable
fun rememberShortDateFormat(): DateTimeFormat<LocalDate> {
    val monthNames = stringArrayResource(Res.array.month_names_short)
    return remember(monthNames) {
        LocalDate.Format {
            day()
            char(' ')
            monthName(MonthNames(monthNames))
            char(' ')
            year()
        }
    }
}

// "18:05" - 24-hour, same as the reminder time picker.
internal val dueTimeFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}
