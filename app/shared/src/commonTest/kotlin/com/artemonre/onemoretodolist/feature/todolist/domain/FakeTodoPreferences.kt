package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTodoPreferences(
    initialArchiveCompletedTodos: Boolean = true
) : TodoPreferences {
    private val archive = MutableStateFlow(initialArchiveCompletedTodos)

    override val archiveCompletedTodos: Flow<Boolean> = archive.asStateFlow()

    override suspend fun setArchiveCompletedTodos(archive: Boolean) {
        this.archive.value = archive
    }
}
