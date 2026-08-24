package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class TodoListViewModel : ViewModel() {

    // Placeholder until feature.todolist.data provides a real repository - the single
    // source of truth for both toggling done and re-sorting.
    private var todos: List<TodoItem> = sampleTodos()

    private val _state = MutableStateFlow(buildState(TodoSortOption.Date))
    val state = _state.asStateFlow()

    private val _events = Channel<TodoListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: TodoListAction) {
        when (action) {
            is TodoListAction.OnTodoClick -> Unit
            is TodoListAction.OnToggleDone -> toggleDone(action.id)
            is TodoListAction.OnSortOptionSelected -> changeSortOption(action.option)
        }
    }

    private fun toggleDone(id: String) {
        todos = todos.map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
        _state.update { buildState(it.sortOption) }
    }

    private fun changeSortOption(option: TodoSortOption) {
        _state.update { buildState(option) }
    }

    private fun buildState(sortOption: TodoSortOption): TodoListState = TodoListState(
        sortOption = sortOption,
        items = todos.sortedByOption(sortOption).map { it.toTodoItemUi() }
    )

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
            date = LocalDate(2026, 8, 25),
            priorityOrder = 0
        )
    )
}
