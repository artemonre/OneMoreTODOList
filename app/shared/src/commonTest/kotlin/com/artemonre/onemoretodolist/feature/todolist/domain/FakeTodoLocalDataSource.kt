package com.artemonre.onemoretodolist.feature.todolist.domain

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeTodoLocalDataSource(
    initialTodos: List<TodoItem> = emptyList()
) : TodoLocalDataSource {
    private val todos = MutableStateFlow(initialTodos)

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

    override suspend fun deleteTodo(id: String): EmptyResult<DataError.Local> {
        todos.update { current -> current.filterNot { it.id == id } }
        return Result.Success(Unit)
    }
}
