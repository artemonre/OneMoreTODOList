package com.artemonre.onemoretodolist.feature.todolist.domain

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface TodoLocalDataSource {
    fun observeTodos(): Flow<List<TodoItem>>
    suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local>
    suspend fun deleteTodo(id: String): EmptyResult<DataError.Local>
}
