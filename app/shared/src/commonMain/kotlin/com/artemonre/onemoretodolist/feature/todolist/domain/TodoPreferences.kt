package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.Flow

interface TodoPreferences {
    val archiveCompletedTodos: Flow<Boolean>
    suspend fun setArchiveCompletedTodos(archive: Boolean)
    val sortOption: Flow<TodoSortOption>
    suspend fun setSortOption(option: TodoSortOption)
}
