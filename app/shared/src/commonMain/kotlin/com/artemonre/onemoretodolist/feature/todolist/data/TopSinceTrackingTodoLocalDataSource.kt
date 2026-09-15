package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.UpdateTopSince
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
        // A genuine edit (lastEditDate actually moved, as opposed to a write that leaves it alone -
        // completing, reordering, a recurrence catch-up) counts as the user dealing with this todo,
        // so it restarts the "how long has this sat at the top" clock even if it never left the top.
        val previousEditDate = delegate.observeTodos().first().firstOrNull { it.id == todo.id }?.lastEditDate
        val wasEdited = previousEditDate != null && previousEditDate != todo.lastEditDate
        val toSave = if (wasEdited) todo.copy(topSince = Clock.System.now()) else todo
        val result = delegate.upsertTodo(toSave)
        if (result is Result.Success) updateTopSince()
        return result
    }

    override suspend fun deleteTodo(id: String): EmptyResult<DataError.Local> {
        val result = delegate.deleteTodo(id)
        if (result is Result.Success) updateTopSince()
        return result
    }
}
