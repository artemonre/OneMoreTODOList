package com.artemonre.onemoretodolist.feature.todolist.domain

fun List<TodoItem>.sortedByOption(option: TodoSortOption): List<TodoItem> = when (option) {
    // Prioritized items first (ranked by priorityOrder), then everything else oldest to newest.
    // The DB query itself has no ORDER BY (see TodoDao), and creationDate is day-granularity, so
    // same-day/same-priority todos would otherwise tie and fall back to whatever row order SQLite
    // happens to return that emission - sortOrder is always globally unique (see
    // TodoPrioritization) so it's a fully deterministic final tiebreaker.
    TodoSortOption.Date -> sortedWith(
        compareBy<TodoItem> { it.priorityOrder == null }
            .thenBy { it.priorityOrder }
            .thenBy { it.creationDate }
            .thenBy { it.sortOrder }
    )
    TodoSortOption.Manual -> sortedBy { it.sortOrder }
    TodoSortOption.Text -> sortedBy { it.text }
    // Most recently completed first.
    TodoSortOption.Archived -> sortedByDescending { it.completionDate }
}
