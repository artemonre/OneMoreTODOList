package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTodoDataSource(private val dao: TodoDao) : TodoLocalDataSource {
    override fun observeTodos(): Flow<List<TodoItem>> =
        dao.observeAll().map { entities -> entities.map { it.toTodoItem() } }

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> =
        runWrite { dao.upsert(todo.toTodoEntity()) }

    override suspend fun deleteTodo(id: String): EmptyResult<DataError.Local> =
        runWrite { dao.markDeleted(id, Clock.System.now().toEpochMilliseconds()) }

    override suspend fun getAllIncludingDeleted(): List<TodoItem> =
        dao.getAllIncludingDeleted().map { it.toTodoItem() }

    override suspend fun upsertVerbatim(todos: List<TodoItem>): EmptyResult<DataError.Local> =
        runWrite { dao.upsertAll(todos.map { it.toTodoEntity() }) }

    override suspend fun purgeDeletedBefore(cutoff: Instant): EmptyResult<DataError.Local> =
        runWrite { dao.purgeDeletedBefore(cutoff.toEpochMilliseconds()) }

    private suspend fun runWrite(write: suspend () -> Unit): EmptyResult<DataError.Local> {
        return try {
            write()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
