package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    // Which sheet/dialog is currently showing, if any - a separate StateFlow from state (todos +
    // sort) so overlay toggles don't ripple into list recomputation, and read from two places:
    // TodoListRoot and TodoListOverlay (see TodoListNavigation.todoListTab()'s overlay slot,
    // needed so the sheet can render above the nav bar and FAB rather than just the tab content).
    private val _overlayState = MutableStateFlow(TodoListOverlayState())
    val overlayState: StateFlow<TodoListOverlayState> = _overlayState.asStateFlow()

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
                _overlayState.update { it.copy(showAddTodoSheet = true) }
            }
            is TodoListAction.OnAddTodoFullScreenClick -> {
                _overlayState.update { it.copy(showAddTodoFullScreenDialog = true) }
            }
            is TodoListAction.OnConfirmAddTodo -> addTodo(action.text, action.isPrioritized)
            is TodoListAction.OnEditTodoClick -> showEditSheet(action.id)
            is TodoListAction.OnConfirmEditTodo -> editTodo(action.id, action.text, action.isPrioritized)
            is TodoListAction.OnDeleteTodo -> deleteTodo(action.id)
            is TodoListAction.OnUndoClick -> undo()
            is TodoListAction.OnDismissOverlay -> _overlayState.value = TodoListOverlayState()
            is TodoListAction.OnShareTodoSwipe -> shareTodo(action.id)
            is TodoListAction.OnCopyFallbackUsed -> viewModelScope.launch {
                _events.send(TodoListEvent.ShowSnackbar("Copied to clipboard"))
            }
        }
    }

    private fun addTodo(text: String, isPrioritized: Boolean) {
        // Closed immediately, same as before - the write below happens in the background rather
        // than the sheet waiting on it.
        _overlayState.update { it.copy(showAddTodoSheet = false, showAddTodoFullScreenDialog = false) }
        viewModelScope.launch {
            addTodoUseCase(text, isPrioritized)
        }
    }

    private fun showEditSheet(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        _overlayState.update { it.copy(editingItem = item.toTodoItemUi()) }
    }

    private fun editTodo(id: String, text: String, isPrioritized: Boolean) {
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
            }
        )
        _overlayState.update { it.copy(editingItem = null) }
        viewModelScope.launch {
            todoLocalDataSource.upsertTodo(updated)
        }
    }

    private fun shareTodo(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        _overlayState.update { it.copy(shareItem = item.toTodoItemUi()) }
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
