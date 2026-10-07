package com.artemonre.onemoretodolist.feature.todolist.domain

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

interface TodoLocalDataSource {
    // Live todos only - soft-deleted ones (deletedAt != null) are hidden.
    fun observeTodos(): Flow<List<TodoItem>>
    suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local>
    // Same as upsertTodo for each of [todos], but as one write - observeTodos() emits once, never
    // a half-applied state in between (e.g. a drag-reorder rewriting several sortOrders).
    suspend fun upsertTodos(todos: List<TodoItem>): EmptyResult<DataError.Local>
    // A soft delete: the todo becomes a tombstone (deletedAt/updatedAt = now), see TodoItem.deletedAt.
    suspend fun deleteTodo(id: String): EmptyResult<DataError.Local>
    // Everything stored, tombstones included - what export, merge and sync compare against.
    suspend fun getAllIncludingDeleted(): List<TodoItem>
    // Writes todos exactly as given, timestamps included, in one transaction - for applying copies
    // that come from elsewhere (an imported file, a Drive backup, the sync server), whose updatedAt
    // must survive as-is. Ordinary edits go through upsertTodo instead.
    suspend fun upsertVerbatim(todos: List<TodoItem>): EmptyResult<DataError.Local>
    suspend fun purgeDeletedBefore(cutoff: Instant): EmptyResult<DataError.Local>
}
