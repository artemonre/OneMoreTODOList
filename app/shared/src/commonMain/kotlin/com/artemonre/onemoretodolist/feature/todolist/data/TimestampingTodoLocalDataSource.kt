package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * The single place [TodoItem.updatedAt] is stamped - sits directly on top of the storage (Room or
 * in-memory), under every other decorator, so no write path can forget it. A write that changes
 * nothing but [TodoItem.topSince] (this device's own bookkeeping, see
 * TopSinceTrackingTodoLocalDataSource) keeps the stored stamp: otherwise that bookkeeping would make
 * a stale local copy look newer than a genuine edit made on another device.
 *
 * [upsertVerbatim] deliberately passes straight through - its callers (import, Drive restore, sync)
 * carry timestamps from elsewhere that must survive unchanged.
 */
class TimestampingTodoLocalDataSource(
    private val delegate: TodoLocalDataSource,
    private val clock: Clock = Clock.System
) : TodoLocalDataSource {
    override fun observeTodos(): Flow<List<TodoItem>> = delegate.observeTodos()

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> {
        val stored = delegate.getAllIncludingDeleted().firstOrNull { it.id == todo.id }
        return delegate.upsertTodo(todo.stamped(stored))
    }

    override suspend fun upsertTodos(todos: List<TodoItem>): EmptyResult<DataError.Local> {
        val storedById = delegate.getAllIncludingDeleted().associateBy { it.id }
        return delegate.upsertTodos(todos.map { it.stamped(storedById[it.id]) })
    }

    private fun TodoItem.stamped(stored: TodoItem?): TodoItem {
        val onlyTopSinceChanged = stored != null &&
            stored.copy(topSince = topSince, updatedAt = updatedAt) == this
        val updatedAt = if (onlyTopSinceChanged && stored != null) stored.updatedAt else clock.now()
        return copy(updatedAt = updatedAt)
    }

    override suspend fun deleteTodo(id: String): EmptyResult<DataError.Local> = delegate.deleteTodo(id)

    override suspend fun getAllIncludingDeleted(): List<TodoItem> = delegate.getAllIncludingDeleted()

    override suspend fun upsertVerbatim(todos: List<TodoItem>): EmptyResult<DataError.Local> =
        delegate.upsertVerbatim(todos)

    override suspend fun purgeDeletedBefore(cutoff: Instant): EmptyResult<DataError.Local> =
        delegate.purgeDeletedBefore(cutoff)
}
