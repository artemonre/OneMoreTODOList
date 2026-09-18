package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import kotlinx.coroutines.flow.Flow

// Wraps a TodoPreferences, invoking onPreferencesChanged after every write - used on Android to
// push an immediate Glance widget refresh when the sort option or archive-completed setting
// changes, mirroring NotifyingThemeRepository/NotifyingTodoLocalDataSource's widget-refresh hooks.
class NotifyingTodoPreferences(
    private val delegate: TodoPreferences,
    private val onPreferencesChanged: suspend () -> Unit
) : TodoPreferences {
    override val archiveCompletedTodos: Flow<Boolean> = delegate.archiveCompletedTodos
    override val sortOption: Flow<TodoSortOption> = delegate.sortOption

    override suspend fun setArchiveCompletedTodos(archive: Boolean) {
        delegate.setArchiveCompletedTodos(archive)
        onPreferencesChanged()
    }

    override suspend fun setSortOption(option: TodoSortOption) {
        delegate.setSortOption(option)
        onPreferencesChanged()
    }
}
