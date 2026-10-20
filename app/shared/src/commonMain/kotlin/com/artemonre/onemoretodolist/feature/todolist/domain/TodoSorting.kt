package com.artemonre.onemoretodolist.feature.todolist.domain

fun List<TodoItem>.sortedByOption(option: TodoSortOption): List<TodoItem> = when (option) {
    // Prioritized items first (ranked by priorityOrder), then everything else oldest to newest.
    // creationDate is day-granularity, so same-day todos are ordered by their exact createdAt -
    // not by sortOrder, which is the Manual position and would let a drag reshuffle Date sort. The
    // DB query itself has no ORDER BY (see TodoDao), so sortOrder (always globally unique, see
    // TodoPrioritization) stays as the final tiebreaker, keeping the order deterministic even if
    // two createdAt values ever match.
    TodoSortOption.Date -> sortedWith(
        compareBy<TodoItem> { it.priorityOrder == null }
            .thenBy { it.priorityOrder }
            .thenBy { it.creationDate }
            .thenBy { it.createdAt }
            .thenBy { it.sortOrder }
    )
    TodoSortOption.Manual -> sortedBy { it.sortOrder }
    TodoSortOption.Text -> sortedBy { it.text }
}
