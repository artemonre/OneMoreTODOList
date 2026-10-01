package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

// Room's web target needs a WebWorkerSQLiteDriver plus a hand-written worker script
// (Room ships no default one). Until that's built, web keeps todos in memory only -
// they don't survive a page reload.
class InMemoryTodoDataSource : TodoLocalDataSource {
    private val todos = MutableStateFlow<List<TodoItem>>(emptyList())

    override fun observeTodos(): Flow<List<TodoItem>> =
        todos.map { all -> all.filter { it.deletedAt == null } }

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> {
        todos.update { current -> current.upserting(listOf(todo)) }
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
        this.todos.update { current -> current.upserting(todos) }
        return Result.Success(Unit)
    }

    override suspend fun purgeDeletedBefore(cutoff: Instant): EmptyResult<DataError.Local> {
        todos.update { current -> current.filterNot { it.deletedAt != null && it.deletedAt < cutoff } }
        return Result.Success(Unit)
    }
}

private fun List<TodoItem>.upserting(incoming: List<TodoItem>): List<TodoItem> {
    val incomingById = incoming.associateBy { it.id }
    val replaced = map { incomingById[it.id] ?: it }
    val existingIds = map { it.id }.toSet()
    return replaced + incoming.filterNot { it.id in existingIds }
}
