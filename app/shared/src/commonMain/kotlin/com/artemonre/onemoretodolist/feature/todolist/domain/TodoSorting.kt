package com.artemonre.onemoretodolist.feature.todolist.domain

fun List<TodoItem>.sortedByOption(option: TodoSortOption): List<TodoItem> = when (option) {
    // Prioritized items first (ranked by priorityOrder), then everything else oldest to newest.
    TodoSortOption.Date -> sortedWith(
        compareBy<TodoItem> { it.priorityOrder == null }
            .thenBy { it.priorityOrder }
            .thenBy { it.creationDate }
    )
    TodoSortOption.Manual -> sortedBy { it.sortOrder }
    TodoSortOption.Text -> sortedBy { it.text }
    // Most recently completed first.
    TodoSortOption.Archived -> sortedByDescending { it.completionDate }
}
