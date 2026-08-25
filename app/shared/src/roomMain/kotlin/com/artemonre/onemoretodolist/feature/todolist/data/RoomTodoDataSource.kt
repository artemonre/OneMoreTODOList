package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTodoDataSource(private val dao: TodoDao) : TodoLocalDataSource {
    override fun observeTodos(): Flow<List<TodoItem>> =
        dao.observeAll().map { entities -> entities.map { it.toTodoItem() } }

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> {
        return try {
            dao.upsert(todo.toTodoEntity())
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
