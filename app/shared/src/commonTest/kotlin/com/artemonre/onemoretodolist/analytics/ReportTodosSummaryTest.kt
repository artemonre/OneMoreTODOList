package com.artemonre.onemoretodolist.analytics

import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class RecordingAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()
    override fun capture(event: AnalyticsEvent) {
        events += event
    }
}

class ReportTodosSummaryTest {
    private val analytics = RecordingAnalytics()
    private val dataSource = FakeTodoLocalDataSource(
        initialTodos = listOf(
            todo("1", TodoStatus.Active),
            todo("2", TodoStatus.Done)
        )
    )
    private val reportTodosSummary = ReportTodosSummary(MapSettings(), dataSource, analytics)

    private fun todo(id: String, status: TodoStatus) = TodoItem(
        id = id,
        text = "Todo $id",
        status = status,
        sortOrder = 0,
        creationDate = LocalDate(2026, 10, 1),
        lastEditDate = LocalDate(2026, 10, 1)
    )

    @Test
    fun `sends one summary a day, over active todos only`() = runTest {
        reportTodosSummary(today = LocalDate(2026, 10, 27))
        reportTodosSummary(today = LocalDate(2026, 10, 27))

        assertEquals(listOf("todos_summary"), analytics.events.map { it.name })
        assertEquals(1, analytics.events.single().properties["active_count"])
    }

    @Test
    fun `sends again on the next day`() = runTest {
        reportTodosSummary(today = LocalDate(2026, 10, 27))
        reportTodosSummary(today = LocalDate(2026, 10, 28))

        assertEquals(2, analytics.events.size)
    }
}
