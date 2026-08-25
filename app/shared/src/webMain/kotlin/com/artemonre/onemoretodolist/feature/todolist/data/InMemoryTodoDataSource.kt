package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Room's web target needs a WebWorkerSQLiteDriver plus a hand-written worker script
// (Room ships no default one). Until that's built, web keeps todos in memory only -
// they don't survive a page reload.
class InMemoryTodoDataSource : TodoLocalDataSource {
    private val todos = MutableStateFlow<List<TodoItem>>(emptyList())

    override fun observeTodos(): Flow<List<TodoItem>> = todos.asStateFlow()

    override suspend fun upsertTodo(todo: TodoItem): EmptyResult<DataError.Local> {
        todos.update { current ->
            if (current.any { it.id == todo.id }) {
                current.map { if (it.id == todo.id) todo else it }
            } else {
                current + todo
            }
        }
        return Result.Success(Unit)
    }
}
