package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag

// Which todos the list shows - Active is the not-yet-completed todos, Done is the
// completed-only view. Independent of TodoSortOption, which only orders the Active view.
enum class TodoListFilter { Active, Done }

data class TodoListState(
    val items: List<TodoItemUi> = emptyList(),
    val sortOption: TodoSortOption = TodoSortOption.Date,
    val filter: TodoListFilter = TodoListFilter.Active,
    // Whether long-press drags todos. Precomputed so the gesture only reads it: true under Manual,
    // and also under another sort whose order happens to match the Manual one - dropping a drag
    // there switches the sort to Manual (see TodoListViewModel.reorder).
    val isReorderEnabled: Boolean = false,
    // All independent of filter/sortOption/items - not just whatever the current view happens to
    // be showing. activeCount is every Active todo (the Active chip); completedCount is every Done
    // one (the Done chip). The doneXCount values count by completionDate for the Completed
    // todos summary card.
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val doneTodayCount: Int = 0,
    val doneThisWeekCount: Int = 0,
    val doneThisMonthCount: Int = 0,
    // Every tag in use on any todo - the form suggests these, reusing their color.
    val knownTags: List<TodoTag> = emptyList()
)
