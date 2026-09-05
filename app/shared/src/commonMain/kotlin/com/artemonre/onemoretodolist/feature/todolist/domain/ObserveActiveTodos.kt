package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Framework-free (no Android/Compose/Glance) so it's reusable by any presentation layer that can
// call into this commonMain module - the todo list screen, the Android home-screen widget, and
// potentially a future native iOS widget extension via the shared Kotlin framework.
class ObserveActiveTodos(
    private val dataSource: TodoLocalDataSource
) {
    operator fun invoke(sortOption: TodoSortOption = TodoSortOption.Date): Flow<List<TodoItem>> =
        dataSource.observeTodos().map { todos ->
            todos.filter { it.status == TodoStatus.Active }.sortedByOption(sortOption)
        }
}
