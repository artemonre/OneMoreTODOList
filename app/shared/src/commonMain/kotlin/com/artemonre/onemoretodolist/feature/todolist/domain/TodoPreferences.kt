package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.Flow

interface TodoPreferences {
    val archiveCompletedTodos: Flow<Boolean>
    suspend fun setArchiveCompletedTodos(archive: Boolean)
}
