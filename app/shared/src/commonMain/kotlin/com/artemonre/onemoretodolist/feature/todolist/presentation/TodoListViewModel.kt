package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L

class TodoListViewModel(
    private val todoLocalDataSource: TodoLocalDataSource,
    private val addTodoUseCase: AddTodo,
    private val toggleTodoDoneUseCase: ToggleTodoDone,
    private val todoPreferences: TodoPreferences
) : ViewModel() {

    private val todos = todoLocalDataSource.observeTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), emptyList())

    val state = combine(todos, todoPreferences.sortOption) { todos, sortOption ->
        val statusFilter = if (sortOption == TodoSortOption.Archived) TodoStatus.Done else TodoStatus.Active
        TodoListState(
            sortOption = sortOption,
            items = todos.filter { it.status == statusFilter }.sortedByOption(sortOption).map { it.toTodoItemUi() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), TodoListState())

    private val _events = Channel<TodoListEvent>()
    val events = _events.receiveAsFlow()

    // The item as it was right before its last completion/deletion, so OnUndoClick can restore
    // it exactly via upsertTodo - holds only the most recent one, matching the single Snackbar
    // that offers Undo for it.
    private var pendingUndoItem: TodoItem? = null

    fun onAction(action: TodoListAction) {
        when (action) {
            is TodoListAction.OnToggleDone -> toggleDone(action.id)
            is TodoListAction.OnSortOptionSelected -> viewModelScope.launch {
                todoPreferences.setSortOption(action.option)
            }
            is TodoListAction.OnReorder -> reorder(action.orderedIds)
            is TodoListAction.OnAddTodoClick -> {
                viewModelScope.launch {
                    _events.send(TodoListEvent.ShowAddTodoSheet)
                }
            }
            is TodoListAction.OnAddTodoFullScreenClick -> {
                viewModelScope.launch {
                    _events.send(TodoListEvent.ShowAddTodoFullScreenDialog)
                }
            }
            is TodoListAction.OnConfirmAddTodo -> addTodo(action.text, action.isPrioritized, action.recurrence)
            is TodoListAction.OnEditTodoClick -> showEditSheet(action.id)
            is TodoListAction.OnConfirmEditTodo -> editTodo(action.id, action.text, action.isPrioritized, action.recurrence)
            is TodoListAction.OnDeleteTodo -> deleteTodo(action.id)
            is TodoListAction.OnUndoClick -> undo()
        }
    }

    private fun addTodo(text: String, isPrioritized: Boolean, recurrence: Recurrence?) {
        viewModelScope.launch {
            addTodoUseCase(text, isPrioritized, recurrence)
        }
    }

    private fun showEditSheet(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            _events.send(TodoListEvent.ShowEditTodoSheet(item.toTodoItemUi()))
        }
    }

    private fun editTodo(id: String, text: String, isPrioritized: Boolean, recurrence: Recurrence?) {
        val currentTodos = todos.value
        val item = currentTodos.firstOrNull { it.id == id } ?: return
        // Only newly-prioritized items jump to the top of Manual sort too - an item that was
        // already prioritized keeps whatever position the user (re)ordered it to.
        val becomingPrioritized = isPrioritized && item.priorityOrder == null
        val updated = item.copy(
            text = text.ifBlank { item.text },
            lastEditDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
            sortOrder = if (becomingPrioritized) topSortOrder(currentTodos) else item.sortOrder,
            priorityOrder = if (isPrioritized) {
                item.priorityOrder ?: prioritize(isPrioritized = true, currentTodos = currentTodos)
            } else {
                null
            },
            recurrence = recurrence,
            recurrenceAnchorDate = recurrenceAnchorDate(item, recurrence)
        )
        viewModelScope.launch {
            todoLocalDataSource.upsertTodo(updated)
        }
    }

    // Editing keeps the existing anchor as long as the Every rule itself is unchanged (same type/
    // interval/unit) - only a genuinely new or changed rule restarts the countdown from today.
    // AfterCompletion needs no anchor at all (it reads completionDate directly).
    private fun recurrenceAnchorDate(item: TodoItem, newRecurrence: Recurrence?): LocalDate? {
        if (newRecurrence?.type != RecurrenceType.Every) return null
        return item.recurrenceAnchorDate.takeIf { item.recurrence == newRecurrence }
            ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    private fun reorder(orderedIds: List<String>) {
        val itemsById = todos.value.associateBy { it.id }
        viewModelScope.launch {
            orderedIds.forEachIndexed { index, id ->
                val item = itemsById[id] ?: return@forEachIndexed
                if (item.sortOrder != index) {
                    todoLocalDataSource.upsertTodo(item.copy(sortOrder = index))
                }
            }
        }
    }

    private fun deleteTodo(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            todoLocalDataSource.deleteTodo(id)
            pendingUndoItem = item
            _events.send(TodoListEvent.ShowUndoSnackbar("Todo deleted"))
        }
    }

    private fun undo() {
        val item = pendingUndoItem ?: return
        pendingUndoItem = null
        viewModelScope.launch {
            todoLocalDataSource.upsertTodo(item)
        }
    }

    private fun prioritize(isPrioritized: Boolean, currentTodos: List<TodoItem>): Double? {
        return if (isPrioritized) {
            (currentTodos.mapNotNull { it.priorityOrder }.minOrNull() ?: 1.0) * 0.9
        } else {
            null
        }
    }

    private fun topSortOrder(currentTodos: List<TodoItem>): Int =
        (currentTodos.minOfOrNull { it.sortOrder } ?: 0) - 1

    private fun toggleDone(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            toggleTodoDoneUseCase(id)
            // Only completing (Active -> Done, or Active -> deleted when archiving is off) is
            // worth offering Undo for - toggling a Done item back to Active is already itself
            // a reversal.
            if (item.status == TodoStatus.Active) {
                pendingUndoItem = item
                _events.send(TodoListEvent.ShowUndoSnackbar("Todo completed"))
            }
        }
    }
}
