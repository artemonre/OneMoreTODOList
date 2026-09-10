package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTodoPreferences(
    initialArchiveCompletedTodos: Boolean = true,
    initialSortOption: TodoSortOption = TodoSortOption.Date
) : TodoPreferences {
    private val archive = MutableStateFlow(initialArchiveCompletedTodos)
    private val sort = MutableStateFlow(initialSortOption)

    override val archiveCompletedTodos: Flow<Boolean> = archive.asStateFlow()

    override suspend fun setArchiveCompletedTodos(archive: Boolean) {
        this.archive.value = archive
    }

    override val sortOption: Flow<TodoSortOption> = sort.asStateFlow()

    override suspend fun setSortOption(option: TodoSortOption) {
        sort.value = option
    }
}
