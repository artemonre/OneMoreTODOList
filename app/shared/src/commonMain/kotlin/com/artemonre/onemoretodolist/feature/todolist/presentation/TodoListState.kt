package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption

// Which todos the list shows - Active is the not-yet-completed todos, Done is the
// completed-only view. Independent of TodoSortOption, which only orders the Active view.
enum class TodoListFilter { Active, Done }

data class TodoListState(
    val items: List<TodoItemUi> = emptyList(),
    val sortOption: TodoSortOption = TodoSortOption.Date,
    val filter: TodoListFilter = TodoListFilter.Active,
    // All independent of filter/sortOption/items - not just whatever the current view happens to
    // be showing. activeCount is every Active todo (the Active chip); completedCount is every Done
    // one (the Done chip). The doneXCount values count by completionDate for the Completed
    // todos summary card.
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val doneTodayCount: Int = 0,
    val doneThisWeekCount: Int = 0,
    val doneThisMonthCount: Int = 0
)
