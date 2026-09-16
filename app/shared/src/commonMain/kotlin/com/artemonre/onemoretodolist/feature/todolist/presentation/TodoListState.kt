package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption

data class TodoListState(
    val items: List<TodoItemUi> = emptyList(),
    val sortOption: TodoSortOption = TodoSortOption.Date,
    // All independent of sortOption/items - not just whatever the current sort/filter happens to
    // be showing. activeCount is every non-archived (Active) todo; archivedCount is every
    // archived (Done) one - the counts row shows one or the other depending on sortOption.
    val activeCount: Int = 0,
    val archivedCount: Int = 0,
    val doneTodayCount: Int = 0,
    val doneThisWeekCount: Int = 0
)
