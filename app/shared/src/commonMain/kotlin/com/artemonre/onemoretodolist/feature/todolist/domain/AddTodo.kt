package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.format.char

// Framework-free (no Android/Compose/Glance) so both the todo list screen and the Android
// home-screen widget's quick-add flow share one "how a new todo is created" implementation,
// rather than duplicating id/date/sort-order/priority logic per caller.
class AddTodo(
    private val dataSource: TodoLocalDataSource
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(text: String, isPrioritized: Boolean) {
        val currentTodos = dataSource.observeTodos().first()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val newItem = TodoItem(
            id = Uuid.random().toString(),
            text = text.ifBlank { defaultText() },
            status = TodoStatus.Active,
            sortOrder = (currentTodos.maxOfOrNull { it.sortOrder } ?: -1) + 1,
            creationDate = today,
            lastEditDate = today,
            priorityOrder = prioritize(isPrioritized, currentTodos)
        )
        dataSource.upsertTodo(newItem)
    }

    private fun prioritize(isPrioritized: Boolean, currentTodos: List<TodoItem>): Double? {
        return if (isPrioritized) {
            (currentTodos.mapNotNull { it.priorityOrder }.minOrNull() ?: 1.0) * 0.9
        } else {
            null
        }
    }

    private fun defaultText(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "Todo added at ${timeFormat.format(now.time)}"
    }
}

private val timeFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}
