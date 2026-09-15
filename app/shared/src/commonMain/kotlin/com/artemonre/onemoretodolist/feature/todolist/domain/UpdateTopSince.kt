package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlinx.coroutines.flow.first

// Keeps TodoItem.topSince in sync with whichever todo is currently top of the active list, under
// whichever sort the user currently has selected - Date, Manual, or Text; "top" doesn't care which
// one is in effect or why an item ended up there (added, completed, reordered, outranked, or the
// sort itself just changed), only that it currently holds that spot. Archived isn't an ordering of
// the active list at all (it's the Done-only view), so it falls back to Date, the same baseline
// used before any sort was ever picked.
//
// Called two ways: reactively after every write, from TopSinceTrackingTodoLocalDataSource (bound
// to its delegate, so its own writes here don't loop back through itself); and explicitly after a
// bare sort-option change, from TodoListViewModel - switching sort alone doesn't touch the todo
// list, so nothing would otherwise trigger a recompute for it.
class UpdateTopSince(
    private val dataSource: TodoLocalDataSource,
    private val todoPreferences: TodoPreferences
) {
    suspend operator fun invoke() {
        val todos = dataSource.observeTodos().first()
        val sortOption = todoPreferences.sortOption.first()
            .takeUnless { it == TodoSortOption.Archived } ?: TodoSortOption.Date
        val topId = todos.filter { it.status == TodoStatus.Active }
            .sortedByOption(sortOption)
            .firstOrNull()?.id
        val now = Clock.System.now()
        for (item in todos) {
            val isTop = item.id == topId
            when {
                isTop && item.topSince == null -> dataSource.upsertTodo(item.copy(topSince = now))
                !isTop && item.topSince != null -> dataSource.upsertTodo(item.copy(topSince = null))
            }
        }
    }
}
