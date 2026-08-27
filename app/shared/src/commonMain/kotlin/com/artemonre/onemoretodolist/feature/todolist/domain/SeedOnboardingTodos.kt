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
                text = "First todo item",
                status = TodoStatus.Active,
                sortOrder = 0,
                creationDate = today,
                lastEditDate = today
            ),
            TodoItem(
                id = Uuid.random().toString(),
                text = "Sorted by date items put from latest to newest",
                status = TodoStatus.Active,
                sortOrder = 1,
                creationDate = today.plus(1, DateTimeUnit.DAY),
                lastEditDate = today.plus(1, DateTimeUnit.DAY)
            ),
            TodoItem(
                id = Uuid.random().toString(),
                text = "If prioritized (Put to top) it goes to the top of the list",
                status = TodoStatus.Active,
                sortOrder = 2,
                creationDate = today,
                lastEditDate = today,
                priorityOrder = 1.0
            )
        ).forEach { todoLocalDataSource.upsertTodo(it) }
    }
}
