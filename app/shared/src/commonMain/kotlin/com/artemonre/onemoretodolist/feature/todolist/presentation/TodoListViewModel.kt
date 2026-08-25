package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class TodoListViewModel : ViewModel() {

    // Placeholder until feature.todolist.data provides a real repository - the single
    // source of truth for both toggling done and re-sorting.
    private var todos: List<TodoItem> = sampleTodos()
    private var nextId: Int = todos.size + 1

    private val _state = MutableStateFlow(buildState(TodoSortOption.Date))
    val state = _state.asStateFlow()

    private val _events = Channel<TodoListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: TodoListAction) {
        when (action) {
            is TodoListAction.OnTodoClick -> Unit
            is TodoListAction.OnToggleDone -> toggleDone(action.id)
            is TodoListAction.OnSortOptionSelected -> changeSortOption(action.option)
            is TodoListAction.OnAddTodoClick -> {
                viewModelScope.launch {
                    _events.send(TodoListEvent.ShowAddTodoSheet)
                }
            }
            is TodoListAction.OnConfirmAddTodo -> addTodo(action.title, action.isPrioritized)
        }
    }

    private fun addTodo(title: String, isPrioritized: Boolean) {
        val newItem = TodoItem(
            id = (nextId++).toString(),
            title = title,
            isDone = false,
            sortOrder = (todos.maxOfOrNull { it.sortOrder } ?: -1) + 1,
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            priorityOrder = if (isPrioritized) {
                (todos.mapNotNull { it.priorityOrder }.maxOrNull() ?: -1) + 1
            } else {
                null
            }
        )
        todos = todos + newItem
        _state.update { buildState(it.sortOption) }
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
