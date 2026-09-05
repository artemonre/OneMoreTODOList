package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import kotlinx.coroutines.flow.Flow

private const val KEY_ARCHIVE_COMPLETED_TODOS = "todo_archive_completed"

@OptIn(ExperimentalSettingsApi::class)
class SettingsTodoPreferences(
    private val settings: ObservableSettings
) : TodoPreferences {
    override val archiveCompletedTodos: Flow<Boolean> =
        settings.getBooleanFlow(KEY_ARCHIVE_COMPLETED_TODOS, true)

    override suspend fun setArchiveCompletedTodos(archive: Boolean) {
        settings.putBoolean(KEY_ARCHIVE_COMPLETED_TODOS, archive)
    }
}
