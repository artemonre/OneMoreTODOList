package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.EditTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.UpdateTopSince
import com.artemonre.onemoretodolist.feature.todolist.domain.knownTags
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.snackbar_reorder_hint
import onemoretodolist.app.shared.generated.resources.snackbar_switched_to_manual_sort
import onemoretodolist.app.shared.generated.resources.snackbar_todo_completed
import onemoretodolist.app.shared.generated.resources.snackbar_todo_deleted

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L

class TodoListViewModel(
    private val todoLocalDataSource: TodoLocalDataSource,
    private val addTodoUseCase: AddTodo,
    private val editTodoUseCase: EditTodo,
    private val toggleTodoDoneUseCase: ToggleTodoDone,
    private val todoPreferences: TodoPreferences,
    private val updateTopSince: UpdateTopSince,
    private val dueTimeScheduler: DueTimeScheduler
) : ViewModel() {

    private val todos = todoLocalDataSource.observeTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), emptyList())

    // Not persisted - the list always opens on Active, unlike sortOption.
    private val filter = MutableStateFlow(TodoListFilter.Active)

    val state = combine(todos, todoPreferences.sortOption, filter) { todos, sortOption, filter ->
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        // ISO week - Monday is day 1, so this is always this week's Monday, even on a Sunday.
        val startOfWeek = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
        val startOfMonth = LocalDate(today.year, today.month, 1)
        val activeTodos = todos.filter { it.status == TodoStatus.Active }
        val items = when (filter) {
            TodoListFilter.Active -> activeTodos.sortedByOption(sortOption)
            // Most recently completed first - sortOption only orders the active list.
            TodoListFilter.Done -> todos.filter { it.status == TodoStatus.Done }.sortedByDescending { it.completionDate }
        }
        // Under another sort, dragging is still allowed while the list already looks exactly like
        // Manual would show it - so switching to Manual on drop changes nothing but the dragged item.
        val isReorderEnabled = filter == TodoListFilter.Active && (
            sortOption == TodoSortOption.Manual ||
                items.map { it.id } == activeTodos.sortedByOption(TodoSortOption.Manual).map { it.id }
            )
        TodoListState(
            sortOption = sortOption,
            filter = filter,
            isReorderEnabled = isReorderEnabled,
            items = items.map { it.toTodoItemUi() },
            activeCount = todos.count { it.status == TodoStatus.Active },
            completedCount = todos.count { it.status == TodoStatus.Done },
            doneTodayCount = todos.count { it.completionDate == today },
            doneThisWeekCount = todos.count { it.completionDate != null && it.completionDate >= startOfWeek },
            doneThisMonthCount = todos.count { it.completionDate != null && it.completionDate >= startOfMonth },
            knownTags = todos.knownTags()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), TodoListState())

    private val _events = Channel<TodoListEvent>()
    val events = _events.receiveAsFlow()

    // The item as it was right before its last completion/deletion, so OnUndoClick can restore
    // it exactly via upsertTodo - holds only the most recent one, matching the single Snackbar
    // that offers Undo for it.
    private var pendingUndoItem: TodoItem? = null

    private val checklistMutex = Mutex()

    fun onAction(action: TodoListAction) {
        when (action) {
            is TodoListAction.OnToggleDone -> toggleDone(action.id)
            is TodoListAction.OnSortOptionSelected -> viewModelScope.launch {
                todoPreferences.setSortOption(action.option)
                // Switching sort alone doesn't touch the todo list, so nothing else would
                // otherwise recompute who's "top" under the newly selected order.
                updateTopSince()
            }
            is TodoListAction.OnFilterSelected -> filter.value = action.filter
            is TodoListAction.OnReorder -> reorder(action.orderedIds)
            is TodoListAction.OnReorderUnavailable -> viewModelScope.launch {
                _events.send(TodoListEvent.ShowSnackbar(Res.string.snackbar_reorder_hint))
            }
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
            is TodoListAction.OnConfirmAddTodo -> addTodo(action.draft)
            is TodoListAction.OnEditTodoClick -> showEditSheet(action.id)
            is TodoListAction.OnConfirmEditTodo -> editTodo(action.id, action.draft)
            is TodoListAction.OnToggleChecklistItem -> toggleChecklistItem(action.todoId, action.itemId)
            is TodoListAction.OnDeleteTodo -> deleteTodo(action.id)
            is TodoListAction.OnUndoClick -> undo()
        }
    }

    private fun addTodo(draft: TodoDraft) {
        viewModelScope.launch {
            addTodoUseCase(
                draft.text,
                draft.isPrioritized,
                draft.recurrence,
                draft.dueDate,
                draft.dueTime,
                draft.dueTimeMode,
                draft.checklist,
                draft.tags
            )
        }
    }

    private fun showEditSheet(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            _events.send(TodoListEvent.ShowEditTodoSheet(item.toTodoItemUi()))
        }
    }

    private fun editTodo(id: String, draft: TodoDraft) {
        viewModelScope.launch {
            editTodoUseCase(
                id,
                draft.text,
                draft.isPrioritized,
                draft.recurrence,
                draft.dueDate,
                draft.dueTime,
                draft.dueTimeMode,
                draft.checklist,
                draft.tags
            )
        }
    }

    // Ticking an item off is a lightweight change, not an edit - lastEditDate and the todo's own
    // status stay as they are (finishing every item doesn't complete the todo). Reads the latest
    // stored todo under a lock rather than todos.value: two quick taps would otherwise both start
    // from the same stale checklist, and the second write would silently undo the first.
    private fun toggleChecklistItem(todoId: String, itemId: String) {
        viewModelScope.launch {
            checklistMutex.withLock {
                val item = todoLocalDataSource.observeTodos().first().firstOrNull { it.id == todoId } ?: return@withLock
                val updated = item.copy(
                    checklist = item.checklist.map { if (it.id == itemId) it.copy(isDone = !it.isDone) else it }
                )
                todoLocalDataSource.upsertTodo(updated)
            }
        }
    }

    private fun reorder(orderedIds: List<String>) {
        val switchesToManual = state.value.sortOption != TodoSortOption.Manual
        // Dropped back in place - only Manual's own sortOrders are worth normalizing then, another
        // sort shouldn't flip to Manual over a drag that moved nothing.
        if (switchesToManual && orderedIds == state.value.items.map { it.id }) return
        val itemsById = todos.value.associateBy { it.id }
        // One batch write, so the list updates once instead of passing through a half-reordered
        // state per changed todo.
        val changed = orderedIds.mapIndexedNotNull { index, id ->
            itemsById[id]?.takeIf { it.sortOrder != index }?.copy(sortOrder = index)
        }
        if (changed.isEmpty()) return
        viewModelScope.launch {
            // Sort first: the drag was only allowed because the Manual order matched the one on
            // screen, so this emits the same items and the dragged list isn't reset to the
            // pre-drag order in between. Writing the new order first would flash it back.
            if (switchesToManual) todoPreferences.setSortOption(TodoSortOption.Manual)
            todoLocalDataSource.upsertTodos(changed)
            if (switchesToManual) {
                updateTopSince()
                _events.send(TodoListEvent.ShowSnackbar(Res.string.snackbar_switched_to_manual_sort))
            }
        }
    }

    private fun deleteTodo(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            todoLocalDataSource.deleteTodo(id)
            dueTimeScheduler.cancel(id)
            pendingUndoItem = item
            _events.send(TodoListEvent.ShowUndoSnackbar(Res.string.snackbar_todo_deleted))
        }
    }

    private fun undo() {
        val item = pendingUndoItem ?: return
        pendingUndoItem = null
        viewModelScope.launch {
            todoLocalDataSource.upsertTodo(item)
            dueTimeScheduler.reschedule(item)
        }
    }

    private fun toggleDone(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            toggleTodoDoneUseCase(id)
            // Only completing (Active -> Done, or Active -> deleted when archiving is off) is
            // worth offering Undo for - toggling a Done item back to Active is already itself
            // a reversal.
            if (item.status == TodoStatus.Active) {
                pendingUndoItem = item
                _events.send(TodoListEvent.ShowUndoSnackbar(Res.string.snackbar_todo_completed))
            }
        }
    }
}
