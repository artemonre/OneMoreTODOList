package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.first

// Framework-free - invoked by the Android alarm receiver when a due-time alarm fires, but has no
// Android/Compose knowledge itself, same convention as ApplyDueRecurrences.
class HandleDueTodoFired(
    private val dataSource: TodoLocalDataSource
) {
    // Returns the updated item (for the caller to use as notification text), or null if there's
    // nothing to notify about anymore - guards the race where the todo was edited, completed, or
    // deleted between when the alarm was armed and when it actually fired.
    suspend operator fun invoke(todoId: String): TodoItem? {
        val currentTodos = dataSource.observeTodos().first()
        val item = currentTodos.firstOrNull { it.id == todoId } ?: return null
        if (item.dueDate == null || item.status != TodoStatus.Active) return null
        val updated = item.copy(
            dueDate = null,
            dueTime = null,
            dueTimeMode = null,
            sortOrder = topSortOrder(currentTodos),
            priorityOrder = topPriorityOrder(currentTodos)
        )
        dataSource.upsertTodo(updated)
        return updated
    }
}
