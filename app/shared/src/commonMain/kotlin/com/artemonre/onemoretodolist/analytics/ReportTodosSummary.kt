package com.artemonre.onemoretodolist.analytics

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.russhwolf.settings.ObservableSettings
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private const val KEY_LAST_TODOS_SUMMARY_DATE = "analytics_last_todos_summary_date"

// todos_summary, at most once a day. Run when the app's UI opens (App()), not from Application -
// on Android the process also starts in the background for the widget, alarms and workers, and
// those days nobody actually opened the app.
class ReportTodosSummary(
    private val settings: ObservableSettings,
    private val todoLocalDataSource: TodoLocalDataSource,
    private val analytics: Analytics
) {
    suspend operator fun invoke(today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())) {
        if (settings.getStringOrNull(KEY_LAST_TODOS_SUMMARY_DATE) == today.toString()) return
        val activeTodos = todoLocalDataSource.observeTodos().first().filter { it.status == TodoStatus.Active }
        analytics.capture(AnalyticsEvents.todosSummary(activeTodos))
        settings.putString(KEY_LAST_TODOS_SUMMARY_DATE, today.toString())
    }
}
