package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class TodoListViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        TodoListState(
            items = sampleTodos().sortedBy { it.sortOrder }.map { it.toTodoItemUi() }
        )
    )
    val state = _state.asStateFlow()

    private val _events = Channel<TodoListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: TodoListAction) {
        when (action) {
            is TodoListAction.OnTodoClick -> Unit
            is TodoListAction.OnToggleDone -> toggleDone(action.id)
        }
    }

    private fun toggleDone(id: String) {
        _state.update { current ->
            current.copy(
                items = current.items.map { item ->
                    if (item.id == id) item.copy(isDone = !item.isDone) else item
                }
            )
        }
    }

    // Placeholder until feature.todolist.data provides a real repository.
    private fun sampleTodos(): List<TodoItem> = listOf(
        TodoItem(
            id = "1",
            title = "Buy groceries",
            isDone = false,
            sortOrder = 0,
            date = LocalDate(2026, 8, 24)
        ),
        TodoItem(
            id = "2",
            title = "Write project architecture",
            isDone = true,
            sortOrder = 1,
            date = LocalDate(2026, 8, 23)
        ),
        TodoItem(
            id = "3",
            title = "Review pull request",
            isDone = false,
            sortOrder = 2,
            date = LocalDate(2026, 8, 25)
        )
    )
}
