package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// Framework-free (no Android/Compose/Glance) so both the todo list screen and the Android
// home-screen widget's checkbox rows share one "toggle done" implementation.
class ToggleTodoDone(
    private val dataSource: TodoLocalDataSource
) {
    suspend operator fun invoke(id: String) {
        val item = dataSource.observeTodos().first().firstOrNull { it.id == id } ?: return
        val newStatus = item.status.toggled()
        val completionDate = if (newStatus == TodoStatus.Done) {
            Clock.System.todayIn(TimeZone.currentSystemDefault())
        } else {
            null
        }
        dataSource.upsertTodo(item.copy(status = newStatus, completionDate = completionDate))
    }
}
