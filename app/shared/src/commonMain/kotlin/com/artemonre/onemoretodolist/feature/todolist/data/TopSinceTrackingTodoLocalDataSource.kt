package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.UpdateTopSince
import kotlinx.coroutines.flow.Flow

/**
 * Wraps a [TodoLocalDataSource], recomputing [TodoItem.topSince] (via [UpdateTopSince]) after every
 * write - covers every write-driven reason a todo could become or stop being top (add, complete,
 * delete, drag-reorder, recurrence catch-up). The one thing a write alone can't cover - the user
 * switching sort option with no other change - is handled separately by TodoListViewModel calling
 * the same [UpdateTopSince] directly. [updateTopSince] is bound to [delegate], not this wrapper, so
 * its own bookkeeping writes never loop back through here.
 */
class TopSinceTrackingTodoLocalDataSource(
    private val delegate: TodoLocalDataSource,
    todoPreferences: TodoPreferences
) : TodoLocalDataSource {
    private val updateTopSince = UpdateTopSince(delegate, todoPreferences)

    override fun observeTodos(): Flow<List<TodoItem>> = delegate.observeTodos()

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> {
        val result = delegate.upsertTodo(todo)
        if (result is Result.Success) updateTopSince()
        return result
    }

    override suspend fun deleteTodo(id: String): EmptyResult<DataError.Local> {
        val result = delegate.deleteTodo(id)
        if (result is Result.Success) updateTopSince()
        return result
    }
}
