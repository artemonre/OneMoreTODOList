package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.format.char

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L

class TodoListViewModel(
    private val todoLocalDataSource: TodoLocalDataSource
) : ViewModel() {

    private val todos = todoLocalDataSource.observeTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), emptyList())

    private val sortOption = MutableStateFlow(TodoSortOption.Date)

    val state = combine(todos, sortOption) { todos, sortOption ->
        TodoListState(
            sortOption = sortOption,
            items = todos.sortedByOption(sortOption).map { it.toTodoItemUi() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), TodoListState())

    private val _events = Channel<TodoListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: TodoListAction) {
        when (action) {
            is TodoListAction.OnTodoClick -> Unit
            is TodoListAction.OnToggleDone -> toggleDone(action.id)
            is TodoListAction.OnSortOptionSelected -> sortOption.value = action.option
            is TodoListAction.OnAddTodoClick -> {
                viewModelScope.launch {
                    _events.send(TodoListEvent.ShowAddTodoSheet)
                }
            }
            is TodoListAction.OnConfirmAddTodo -> addTodo(action.title, action.isPrioritized)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun addTodo(title: String, isPrioritized: Boolean) {
        val currentTodos = todos.value
        val newItem = TodoItem(
            id = Uuid.random().toString(),
            title = title.ifBlank { defaultTitle() },
            isDone = false,
            sortOrder = (currentTodos.maxOfOrNull { it.sortOrder } ?: -1) + 1,
            date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            priorityOrder = if (isPrioritized) {
                (currentTodos.mapNotNull { it.priorityOrder }.minOrNull() ?: 1.0) * 0.9
            } else {
                null
            }
        )
        viewModelScope.launch {
            todoLocalDataSource.upsertTodo(newItem)
        }
    }

    private fun defaultTitle(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "Todo added at ${timeFormat.format(now.time)}"
    }

    private fun toggleDone(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            todoLocalDataSource.upsertTodo(item.copy(isDone = !item.isDone))
        }
    }
}

private val timeFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}
