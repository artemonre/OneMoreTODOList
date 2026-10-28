package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.analytics.Analytics
import com.artemonre.onemoretodolist.analytics.AnalyticsEvents
import com.artemonre.onemoretodolist.analytics.AnalyticsSource
import com.artemonre.onemoretodolist.analytics.NoOpAnalytics
import com.artemonre.onemoretodolist.analytics.ScannedTextChoice
import com.artemonre.onemoretodolist.analytics.TodoForm
import com.artemonre.onemoretodolist.analytics.UndoneAction
import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.EditTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.HomeScreenWidget
import com.artemonre.onemoretodolist.feature.todolist.domain.NoHomeScreenWidget
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.UpdateTopSince
import com.artemonre.onemoretodolist.feature.todolist.domain.knownTags
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import com.artemonre.onemoretodolist.isPhotoTextCaptureSupported
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
import onemoretodolist.app.shared.generated.resources.snackbar_no_text_found
import onemoretodolist.app.shared.generated.resources.snackbar_reorder_hint
import onemoretodolist.app.shared.generated.resources.snackbar_switched_to_manual_sort
import onemoretodolist.app.shared.generated.resources.snackbar_todo_completed
import onemoretodolist.app.shared.generated.resources.snackbar_todo_deleted

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L
// setting_changed's screen value for a setting changed from the list (the empty Done list's button).
private const val TODO_LIST_SCREEN = "Todo list"

private data class PickedTip(val tip: EmptyStateTip, val canPinWidget: Boolean)

class TodoListViewModel(
    private val todoLocalDataSource: TodoLocalDataSource,
    private val addTodoUseCase: AddTodo,
    private val editTodoUseCase: EditTodo,
    private val toggleTodoDoneUseCase: ToggleTodoDone,
    private val todoPreferences: TodoPreferences,
    private val updateTopSince: UpdateTopSince,
    private val dueTimeScheduler: DueTimeScheduler,
    private val homeScreenWidget: HomeScreenWidget = NoHomeScreenWidget,
    private val analytics: Analytics = NoOpAnalytics
) : ViewModel() {

    private val todos = todoLocalDataSource.observeTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), emptyList())

    // Not persisted - the list always opens on Active, unlike sortOption.
    private val filter = MutableStateFlow(TodoListFilter.Active)

    // Picked once in init, so it stays the same tip for as long as the app is open.
    private val emptyStateTip = MutableStateFlow<PickedTip?>(null)

    // empty_state_tip_shown goes out once per launch, not every time the empty list reappears.
    private var isEmptyStateTipReported = false

    init {
        viewModelScope.launch { emptyStateTip.value = pickEmptyStateTip() }
    }

    val state = combine(
        todos,
        todoPreferences.sortOption,
        filter,
        todoPreferences.archiveCompletedTodos,
        emptyStateTip
    ) { todos, sortOption, filter, archiveCompletedTodos, emptyStateTip ->
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
            knownTags = todos.knownTags(),
            archiveCompletedTodos = archiveCompletedTodos,
            emptyStateTip = emptyStateTip?.tip,
            canPinWidget = emptyStateTip?.canPinWidget == true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), TodoListState())

    private val _events = Channel<TodoListEvent>()
    val events = _events.receiveAsFlow()

    // The item as it was right before its last completion/deletion, so OnUndoClick can restore
    // it exactly via upsertTodo - holds only the most recent one, matching the single Snackbar
    // that offers Undo for it.
    private var pendingUndoItem: TodoItem? = null
    private var pendingUndoAction: UndoneAction? = null

    private val checklistMutex = Mutex()

    fun onAction(action: TodoListAction) {
        when (action) {
            is TodoListAction.OnToggleDone -> toggleDone(action.id)
            is TodoListAction.OnSortOptionSelected -> viewModelScope.launch {
                todoPreferences.setSortOption(action.option)
                analytics.capture(AnalyticsEvents.listSorted(action.option))
                // Switching sort alone doesn't touch the todo list, so nothing else would
                // otherwise recompute who's "top" under the newly selected order.
                updateTopSince()
            }
            is TodoListAction.OnFilterSelected -> {
                filter.value = action.filter
                analytics.capture(AnalyticsEvents.listFiltered(action.filter))
            }
            is TodoListAction.OnReorder -> reorder(action.orderedIds)
            is TodoListAction.OnReorderUnavailable -> viewModelScope.launch {
                analytics.capture(AnalyticsEvents.reorderUnavailableShown())
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
            is TodoListAction.OnConfirmAddTodo -> addTodo(action.draft, action.form)
            is TodoListAction.OnEditTodoClick -> showEditSheet(action.id)
            is TodoListAction.OnConfirmEditTodo -> editTodo(action.id, action.draft)
            is TodoListAction.OnToggleChecklistItem -> toggleChecklistItem(action.todoId, action.itemId)
            is TodoListAction.OnDeleteTodo -> deleteTodo(action.id)
            is TodoListAction.OnUndoClick -> undo()
            is TodoListAction.OnCapturePhotoClick -> viewModelScope.launch {
                analytics.capture(AnalyticsEvents.photoCaptureOpened())
                _events.send(TodoListEvent.ShowPhotoCapture)
            }
            is TodoListAction.OnTextRecognized -> viewModelScope.launch {
                _events.send(
                    if (action.paragraphs.isEmpty()) {
                        TodoListEvent.ShowSnackbar(Res.string.snackbar_no_text_found)
                    } else {
                        TodoListEvent.ShowScannedTextReview(action.paragraphs)
                    }
                )
            }
            is TodoListAction.OnConfirmAddScannedTodos -> {
                analytics.capture(AnalyticsEvents.scannedTextConfirmed(ScannedTextChoice.Separate))
                addScannedTodos(action.lines)
            }
            is TodoListAction.OnScannedTextContinuedAsSingle ->
                analytics.capture(AnalyticsEvents.scannedTextConfirmed(ScannedTextChoice.Single))
            is TodoListAction.OnScannedTextRetaken -> analytics.capture(AnalyticsEvents.scannedTextRetaken())
            is TodoListAction.OnKeepCompletedTodosClick -> viewModelScope.launch {
                todoPreferences.setArchiveCompletedTodos(true)
                analytics.capture(AnalyticsEvents.keepCompletedEnabled())
                // The same setting the Settings switch changes, so settings dashboards see it too.
                analytics.capture(
                    AnalyticsEvents.settingChanged(
                        screen = TODO_LIST_SCREEN,
                        section = "Archive completed todos",
                        option = "On"
                    )
                )
            }
            is TodoListAction.OnAddWidgetClick -> viewModelScope.launch {
                analytics.capture(AnalyticsEvents.widgetPinRequested(shown = homeScreenWidget.requestPin()))
            }
            is TodoListAction.OnAddFormExpanded -> analytics.capture(AnalyticsEvents.addFormExpanded())
            is TodoListAction.OnTodoDetailsOpened -> analytics.capture(AnalyticsEvents.todoDetailsOpened())
            is TodoListAction.OnTodoShared -> analytics.capture(AnalyticsEvents.todoShared(action.method))
            is TodoListAction.OnEmptyStateTipShown -> {
                val tip = state.value.emptyStateTip
                if (tip != null && !isEmptyStateTipReported) {
                    isEmptyStateTipReported = true
                    analytics.capture(AnalyticsEvents.emptyStateTipShown(tip))
                }
            }
        }
    }

    // Only tips that apply here: the photo capture behind a long-press on +, the widget while none
    // is placed yet, and the full form behind the quick-add sheet's "More settings".
    private suspend fun pickEmptyStateTip(): PickedTip {
        val tips = buildList {
            if (isPhotoTextCaptureSupported) add(EmptyStateTip.HoldAddButton)
            if (homeScreenWidget.shouldSuggest()) add(EmptyStateTip.AddWidget)
            add(EmptyStateTip.MoreSettings)
        }
        return PickedTip(tips.random(), homeScreenWidget.canRequestPin())
    }

    private fun addTodo(draft: TodoDraft, form: TodoForm) {
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
            analytics.capture(AnalyticsEvents.todoCreated(AnalyticsSource.App, form))
        }
    }

    // One plain todo per line, in the order they were read - sequential (not parallel) so each one
    // lands after the previous in Manual sort, see AddTodo's sortOrder.
    private fun addScannedTodos(lines: List<String>) {
        viewModelScope.launch {
            lines.forEach { line ->
                addTodoUseCase(text = line, isPrioritized = false)
                analytics.capture(AnalyticsEvents.todoCreated(AnalyticsSource.App, TodoForm.Scanned))
            }
        }
    }

    private fun showEditSheet(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            _events.send(TodoListEvent.ShowEditTodoSheet(item.toTodoItemUi()))
        }
    }

    private fun editTodo(id: String, draft: TodoDraft) {
        // As it was before the edit - the edit itself can move it off the top, or onto it.
        val item = todos.value.firstOrNull { it.id == id }
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
            item?.let { analytics.capture(AnalyticsEvents.todoEdited(AnalyticsSource.App, it)) }
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
                analytics.capture(AnalyticsEvents.checklistItemToggled())
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
            analytics.capture(AnalyticsEvents.todosReordered(switchedToManual = switchesToManual))
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
            analytics.capture(AnalyticsEvents.todoDeleted(AnalyticsSource.App, filter.value, item))
            pendingUndoItem = item
            pendingUndoAction = UndoneAction.Deleted
            _events.send(TodoListEvent.ShowUndoSnackbar(Res.string.snackbar_todo_deleted))
        }
    }

    private fun undo() {
        val item = pendingUndoItem ?: return
        val undoneAction = pendingUndoAction
        pendingUndoItem = null
        pendingUndoAction = null
        viewModelScope.launch {
            todoLocalDataSource.upsertTodo(item)
            dueTimeScheduler.reschedule(item)
            undoneAction?.let { analytics.capture(AnalyticsEvents.todoUndone(it)) }
        }
    }

    private fun toggleDone(id: String) {
        val item = todos.value.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            // Read before the toggle - the same setting ToggleTodoDone decides archive-or-delete by.
            val archived = todoPreferences.archiveCompletedTodos.first()
            toggleTodoDoneUseCase(id)
            analytics.capture(
                if (item.status == TodoStatus.Active) {
                    AnalyticsEvents.todoCompleted(AnalyticsSource.App, archived, item)
                } else {
                    AnalyticsEvents.todoReopened(AnalyticsSource.App, item)
                }
            )
            // Only completing (Active -> Done, or Active -> deleted when archiving is off) is
            // worth offering Undo for - toggling a Done item back to Active is already itself
            // a reversal.
            if (item.status == TodoStatus.Active) {
                pendingUndoItem = item
                pendingUndoAction = UndoneAction.Completed
                _events.send(TodoListEvent.ShowUndoSnackbar(Res.string.snackbar_todo_completed))
            }
        }
    }
}
