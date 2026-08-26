package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

// Runs once at app startup (see App.kt) rather than from a ViewModel's init block,
// so it stays a plain, independently testable unit instead of an untestable side effect.
class SeedOnboardingTodos(
    private val todoLocalDataSource: TodoLocalDataSource
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke() {
        if (todoLocalDataSource.observeTodos().first().isNotEmpty()) return

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        listOf(
            TodoItem(
                id = Uuid.random().toString(),
                title = "First todo item",
                isDone = false,
                sortOrder = 0,
                date = today
            ),
            TodoItem(
                id = Uuid.random().toString(),
                title = "Sorted by date items put from latest to newest",
                isDone = false,
                sortOrder = 1,
                date = today.plus(1, DateTimeUnit.DAY)
            ),
            TodoItem(
                id = Uuid.random().toString(),
                title = "If prioritized (Put to top) it goes to the top of the list",
                isDone = false,
                sortOrder = 2,
                date = today,
                priorityOrder = 1.0
            )
        ).forEach { todoLocalDataSource.upsertTodo(it) }
    }
}
