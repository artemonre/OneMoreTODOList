package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val KEY_ARCHIVE_COMPLETED_TODOS = "todo_archive_completed"
private const val KEY_SORT_OPTION = "todo_sort_option"

@OptIn(ExperimentalSettingsApi::class)
class SettingsTodoPreferences(
    private val settings: ObservableSettings
) : TodoPreferences {
    override val archiveCompletedTodos: Flow<Boolean> =
        settings.getBooleanFlow(KEY_ARCHIVE_COMPLETED_TODOS, true)

    override suspend fun setArchiveCompletedTodos(archive: Boolean) {
        settings.putBoolean(KEY_ARCHIVE_COMPLETED_TODOS, archive)
    }

    override val sortOption: Flow<TodoSortOption> =
        settings.getStringFlow(KEY_SORT_OPTION, TodoSortOption.Date.name)
            .map { name -> TodoSortOption.entries.firstOrNull { it.name == name } ?: TodoSortOption.Date }

    override suspend fun setSortOption(option: TodoSortOption) {
        settings.putString(KEY_SORT_OPTION, option.name)
    }
}
