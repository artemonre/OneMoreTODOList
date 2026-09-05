package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Wraps a [TodoLocalDataSource], invoking [onDataChanged] after every successful write. Used on
 * Android to push an immediate Glance widget refresh after a write made outside the widget itself
 * (e.g. from the app, or the widget's own quick-add popup) - a widget's own actions already get
 * redrawn automatically by Glance, but nothing else does, and the widget's passive Flow collection
 * isn't reliably kept alive by the OS to pick such writes up promptly on its own.
 */
class NotifyingTodoLocalDataSource(
    private val delegate: TodoLocalDataSource,
    private val onDataChanged: suspend () -> Unit
) : TodoLocalDataSource {
    override fun observeTodos(): Flow<List<TodoItem>> = delegate.observeTodos()

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> {
        val result = delegate.upsertTodo(todo)
        if (result is Result.Success) onDataChanged()
        return result
    }

    override suspend fun deleteTodo(id: String): EmptyResult<DataError.Local> {
        val result = delegate.deleteTodo(id)
        if (result is Result.Success) onDataChanged()
        return result
    }
}
