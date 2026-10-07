package com.artemonre.onemoretodolist.feature.todolist.domain

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// Mirrors the real storage contract: deletes are soft (tombstones hidden from observeTodos).
class FakeTodoLocalDataSource(
    initialTodos: List<TodoItem> = emptyList()
) : TodoLocalDataSource {
    private val todos = MutableStateFlow(initialTodos)

    override fun observeTodos(): Flow<List<TodoItem>> =
        todos.map { all -> all.filter { it.deletedAt == null } }

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> {
        upsertAll(listOf(todo))
        return Result.Success(Unit)
    }

    override suspend fun upsertTodos(todos: List<TodoItem>): EmptyResult<DataError.Local> {
        upsertAll(todos)
        return Result.Success(Unit)
    }

    override suspend fun deleteTodo(id: String): EmptyResult<DataError.Local> {
        val now = Clock.System.now()
        todos.update { current ->
            current.map { if (it.id == id) it.copy(deletedAt = now, updatedAt = now) else it }
        }
        return Result.Success(Unit)
    }

    override suspend fun getAllIncludingDeleted(): List<TodoItem> = todos.value

    override suspend fun upsertVerbatim(todos: List<TodoItem>): EmptyResult<DataError.Local> {
        upsertAll(todos)
        return Result.Success(Unit)
    }

    override suspend fun purgeDeletedBefore(cutoff: Instant): EmptyResult<DataError.Local> {
        todos.update { current -> current.filterNot { it.deletedAt != null && it.deletedAt < cutoff } }
        return Result.Success(Unit)
    }

    private fun upsertAll(incoming: List<TodoItem>) {
        todos.update { current ->
            val incomingById = incoming.associateBy { it.id }
            val existingIds = current.map { it.id }.toSet()
            current.map { incomingById[it.id] ?: it } + incoming.filterNot { it.id in existingIds }
        }
    }
}
