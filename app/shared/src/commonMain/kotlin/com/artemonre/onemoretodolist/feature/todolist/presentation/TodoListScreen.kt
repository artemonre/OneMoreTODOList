package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.analytics.ShareMethod
import com.artemonre.onemoretodolist.analytics.TodoForm
import com.artemonre.onemoretodolist.core.container.FAB_NAV_BAR_OVERLAP
import com.artemonre.onemoretodolist.core.designsystem.components.AppCard
import com.artemonre.onemoretodolist.core.designsystem.components.AppChipGroup
import com.artemonre.onemoretodolist.core.designsystem.components.AppFab
import com.artemonre.onemoretodolist.core.designsystem.components.CLOUD_SEGMENT_COUNT
import com.artemonre.onemoretodolist.core.designsystem.components.CloudSegment
import com.artemonre.onemoretodolist.core.designsystem.components.appListItemCardShape
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalActionPlacement
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.core.presentation.ObserveAsEvents
import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.PhotoTextCaptureDialog
import com.artemonre.onemoretodolist.createPlainTextClipEntry
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.rememberNativeShareLauncher
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.action_delete
import onemoretodolist.app.shared.generated.resources.action_edit
import onemoretodolist.app.shared.generated.resources.action_move_down
import onemoretodolist.app.shared.generated.resources.action_move_to_bottom
import onemoretodolist.app.shared.generated.resources.action_move_to_top
import onemoretodolist.app.shared.generated.resources.action_move_up
import onemoretodolist.app.shared.generated.resources.action_undo
import onemoretodolist.app.shared.generated.resources.completed_summary_this_month
import onemoretodolist.app.shared.generated.resources.completed_summary_this_week
import onemoretodolist.app.shared.generated.resources.completed_summary_title
import onemoretodolist.app.shared.generated.resources.completed_summary_today
import onemoretodolist.app.shared.generated.resources.mascot_sitting
import onemoretodolist.app.shared.generated.resources.mascot_sitting_monochrome
import onemoretodolist.app.shared.generated.resources.mascot_ball_monochrome
import onemoretodolist.app.shared.generated.resources.mascot_ball
import onemoretodolist.app.shared.generated.resources.share_title
import onemoretodolist.app.shared.generated.resources.snackbar_copied
import onemoretodolist.app.shared.generated.resources.sort_date
import onemoretodolist.app.shared.generated.resources.sort_manual
import onemoretodolist.app.shared.generated.resources.sort_text
import onemoretodolist.app.shared.generated.resources.todo_filter_active
import onemoretodolist.app.shared.generated.resources.todo_filter_done
import onemoretodolist.app.shared.generated.resources.todo_list_empty_active
import onemoretodolist.app.shared.generated.resources.todo_list_empty_completed
import onemoretodolist.app.shared.generated.resources.todo_list_add_widget
import onemoretodolist.app.shared.generated.resources.todo_list_empty_completed_not_archived
import onemoretodolist.app.shared.generated.resources.todo_list_keep_completed
import onemoretodolist.app.shared.generated.resources.todo_list_tip_hold_add
import onemoretodolist.app.shared.generated.resources.todo_list_tip_more_settings
import onemoretodolist.app.shared.generated.resources.todo_list_tip_widget
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val SCROLL_TO_TOP_THRESHOLD = 10
private const val DRAG_ROTATION_DEGREES = 4f
private val SWIPE_ACTION_WIDTH = 56.dp

// Clears the scroll-to-top button (56dp) plus its 16dp margin, with a little extra breathing
// room, so the last list item never ends up hidden behind it after a full scroll.
private val LIST_BOTTOM_CONTENT_PADDING = 80.dp
private val MASCOT_HEIGHT = 160.dp
// Half of ButtonDefaults.ContentPadding (24dp/8dp), snapped to the spacing scale.
private val SORT_BUTTON_CONTENT_PADDING = PaddingValues(horizontal = AppSpacing.m, vertical = AppSpacing.xs)
private const val BALL_AFTER_TODO_COUNT = 5
// The ball drawings have no margin around the ball, so this is the ball's own size.
private val BALL_SIZE = 36.dp
// The + straddles the nav bar's top edge, reaching FAB_NAV_BAR_OVERLAP up into this screen - the
// hold-+ tip clears that plus a small gap.
private val HOLD_ADD_TIP_BOTTOM_PADDING = FAB_NAV_BAR_OVERLAP + AppSpacing.s

private enum class SwipeAnchor { Closed, Open, ShareTrigger }

// One row of the non-empty list. The ball is a slot in the same reorderable list as the todos, so
// dragging a todo past it swaps with it like with any other row - instead of the ball being a
// fixed, non-reorderable gap the drag has to jump across.
private sealed interface ListEntry {
    val key: Any

    data class Todo(val item: TodoItemUi) : ListEntry {
        override val key: Any get() = item.id
    }

    data object Ball : ListEntry {
        override val key: Any = "ball"
    }
}

// The ball sits after the first BALL_AFTER_TODO_COUNT todos, or after all of them when there are
// fewer.
private fun List<TodoItemUi>.withBall(): List<ListEntry> =
    take(BALL_AFTER_TODO_COUNT).map(ListEntry::Todo) + ListEntry.Ball + drop(BALL_AFTER_TODO_COUNT).map(ListEntry::Todo)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun TodoListRoot(
    viewModel: TodoListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingTodo by remember { mutableStateOf<TodoItemUi?>(null) }
    var showAddTodoSheet by remember { mutableStateOf(false) }
    var showAddTodoFullScreenDialog by remember { mutableStateOf(false) }
    // Text carried from the quick-add sheet's "More settings" button into the full-screen dialog -
    // cleared whenever the full-screen dialog is opened directly, so that path never inherits a
    // stale draft left over from an earlier "More settings" hop.
    var addTodoDraftText by remember { mutableStateOf("") }
    // Same idea for the checklist - only ever non-empty when a scanned photo is continued as one
    // todo from ScannedTextReviewDialog.
    var addTodoDraftChecklist by remember { mutableStateOf(emptyList<ChecklistItem>()) }
    var showPhotoCapture by remember { mutableStateOf(false) }
    var scannedParagraphs by remember { mutableStateOf<List<ScannedParagraph>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TodoListEvent.ShowAddTodoSheet -> showAddTodoSheet = true
            TodoListEvent.ShowAddTodoFullScreenDialog -> {
                addTodoDraftText = ""
                addTodoDraftChecklist = emptyList()
                showAddTodoFullScreenDialog = true
            }
            is TodoListEvent.ShowEditTodoSheet -> editingTodo = event.item
            TodoListEvent.ShowPhotoCapture -> showPhotoCapture = true
            is TodoListEvent.ShowScannedTextReview -> scannedParagraphs = event.paragraphs
            is TodoListEvent.ShowSnackbar -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(message = getString(event.message), duration = SnackbarDuration.Short)
                }
            }
            is TodoListEvent.ShowUndoSnackbar -> {
                coroutineScope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = getString(event.message),
                        actionLabel = getString(Res.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onAction(TodoListAction.OnUndoClick)
                    }
                }
            }
            is TodoListEvent.ShowSnackbar -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(getString(event.message))
                }
            }
        }
    }

    TodoListScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )

    if (showAddTodoSheet) {
        TodoFormBottomSheet(
            editingItem = null,
            onConfirm = { draft ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(draft, TodoForm.QuickSheet))
                showAddTodoSheet = false
            },
            onDismiss = { showAddTodoSheet = false },
            onMoreSettingsClick = { draftText ->
                viewModel.onAction(TodoListAction.OnAddFormExpanded)
                showAddTodoSheet = false
                addTodoDraftText = draftText
                addTodoDraftChecklist = emptyList()
                showAddTodoFullScreenDialog = true
            }
        )
    }

    if (showAddTodoFullScreenDialog) {
        TodoFormFullScreenDialog(
            editingItem = null,
            onConfirm = { draft ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(draft, TodoForm.FullForm))
                showAddTodoFullScreenDialog = false
            },
            onDismiss = { showAddTodoFullScreenDialog = false },
            initialText = addTodoDraftText,
            initialChecklist = addTodoDraftChecklist,
            knownTags = state.knownTags
        )
    }

    if (showPhotoCapture) {
        PhotoTextCaptureDialog(
            onTextRecognized = { paragraphs ->
                showPhotoCapture = false
                viewModel.onAction(TodoListAction.OnTextRecognized(paragraphs))
            },
            onDismiss = { showPhotoCapture = false }
        )
    }

    scannedParagraphs?.let { paragraphs ->
        ScannedTextReviewDialog(
            paragraphs = paragraphs,
            onConfirmSeparate = { confirmedLines ->
                viewModel.onAction(TodoListAction.OnConfirmAddScannedTodos(confirmedLines))
                scannedParagraphs = null
            },
            onContinueAsSingle = { text, checklist ->
                viewModel.onAction(TodoListAction.OnScannedTextContinuedAsSingle)
                scannedParagraphs = null
                addTodoDraftText = text
                addTodoDraftChecklist = checklist.map { ChecklistItem(id = Uuid.random().toString(), text = it) }
                showAddTodoFullScreenDialog = true
            },
            // Back/Discard steps back to the camera for another shot rather than leaving the flow.
            onDismiss = {
                viewModel.onAction(TodoListAction.OnScannedTextRetaken)
                scannedParagraphs = null
                showPhotoCapture = true
            }
        )
    }

    editingTodo?.let { item ->
        TodoFormFullScreenDialog(
            editingItem = item,
            onConfirm = { draft ->
                viewModel.onAction(TodoListAction.OnConfirmEditTodo(item.id, draft))
                editingTodo = null
            },
            onDismiss = { editingTodo = null },
            knownTags = state.knownTags
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TodoListScreen(
    state: TodoListState,
    onAction: (TodoListAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= SCROLL_TO_TOP_THRESHOLD }
    }
    val actionAlignment = when (LocalActionPlacement.current) {
        ActionPlacement.Start -> Alignment.BottomStart
        ActionPlacement.End -> Alignment.BottomEnd
    }
    // By id, not a snapshot, so the open dialog follows live changes (like ticking checklist items)
    // and closes on its own if the todo leaves the current list.
    var detailItemId by remember { mutableStateOf<String?>(null) }
    var shareItem by remember { mutableStateOf<TodoItemUi?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val nativeShareLauncher = rememberNativeShareLauncher()
    val clipboard = LocalClipboard.current

    // The reorderable library needs a local mutable list it can shuffle live during a drag.
    // It's re-synced from state.items whenever that changes - which OnReorder itself never
    // triggers mid-drag (it's only dispatched on drag-stop), so this can't fight an in-progress
    // drag; it only picks up genuinely external changes (add/edit/delete/toggle elsewhere).
    // Where across the list the ball lies, -1 (start) to 1 (end) - a new spot each time the screen
    // is opened, kept across scrolling and rotation.
    val ballHorizontalBias = rememberSaveable { Random.nextFloat() * 2 - 1 }
    var listEntries by remember { mutableStateOf(state.items.withBall()) }
    LaunchedEffect(state.items) { listEntries = state.items.withBall() }

    // Each todo's slice of the shared cloud texture, by its position among the todos (the ball
    // doesn't count). CLOUD_SEGMENT_COUNT matches BALL_AFTER_TODO_COUNT, so the first cloud ends
    // right above the ball. Follows listEntries, so it re-forms live around a dragged todo.
    val cloudSegments = remember(listEntries) {
        listEntries.filterIsInstance<ListEntry.Todo>().withIndex().associate { (index, entry) ->
            entry.item.id to CloudSegment(
                cloudIndex = index / CLOUD_SEGMENT_COUNT,
                segment = index % CLOUD_SEGMENT_COUNT
            )
        }
    }

    // from.index/to.index are positions in the whole LazyColumn, not in listEntries - the
    // summary card and filter/sort header shift them. Look entries up by key instead of trusting
    // the raw index.
    val reorderableListState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = listEntries.indexOfFirst { it.key == from.key }
        val toIndex = listEntries.indexOfFirst { it.key == to.key }
        if (fromIndex != -1 && toIndex != -1) {
            val swapped = listEntries.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
            // Crossing the ball is a plain swap, so the dragged todo moves past it smoothly. A swap
            // with another todo puts the ball back after BALL_AFTER_TODO_COUNT todos, so when the
            // ball was off by one, the swapped neighbour goes through it (two slots) instead - and
            // it never needs to snap back in one big move.
            listEntries = if (to.key == ListEntry.Ball.key) {
                swapped
            } else {
                swapped.filterIsInstance<ListEntry.Todo>().map { it.item }.withBall()
            }
        }
    }

    // Every tip renders exactly while the Active list is empty - under the mascot, or above the +.
    if (state.items.isEmpty() && state.filter == TodoListFilter.Active && state.emptyStateTip != null) {
        LaunchedEffect(state.emptyStateTip) { onAction(TodoListAction.OnEmptyStateTipShown) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Top)
                .add(WindowInsets(top = AppSpacing.s, bottom = LIST_BOTTOM_CONTENT_PADDING))
                .asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
        ) {
            item(key = "completed_summary") {
                CompletedSummaryCard(
                    todayCount = state.doneTodayCount,
                    thisWeekCount = state.doneThisWeekCount,
                    thisMonthCount = state.doneThisMonthCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.s)
                )
            }

            item(key = "filter_and_sort") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Pinned to the sort button's tap-target height, so the row doesn't change
                        // size when the button disappears on the Done filter.
                        .heightIn(min = LocalMinimumInteractiveComponentSize.current)
                        .padding(horizontal = AppSpacing.s),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeLabel = stringResource(Res.string.todo_filter_active, state.activeCount)
                    val doneLabel = stringResource(Res.string.todo_filter_done, state.completedCount)
                    AppChipGroup(
                        options = TodoListFilter.entries,
                        selectedOption = state.filter,
                        onOptionSelected = { onAction(TodoListAction.OnFilterSelected(it)) },
                        label = { filter ->
                            when (filter) {
                                TodoListFilter.Active -> activeLabel
                                TodoListFilter.Done -> doneLabel
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        compact = true
                    )
                    // Done is always most-recently-completed first, so sorting only applies
                    // to Active.
                    if (state.filter == TodoListFilter.Active) {
                        Box {
                            FilledTonalButton(
                                onClick = { sortMenuExpanded = true },
                                contentPadding = SORT_BUTTON_CONTENT_PADDING
                            ) {
                                Text(stringResource(state.sortOption.displayName()))
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                TodoSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(option.displayName())) },
                                        onClick = {
                                            onAction(TodoListAction.OnSortOptionSelected(option))
                                            sortMenuExpanded = false
                                        },
                                        trailingIcon = if (option == state.sortOption) {
                                            { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
                                        } else {
                                            null
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val todoRow: @Composable LazyItemScope.(TodoItemUi) -> Unit = { item ->
                ReorderableItem(reorderableListState, key = item.id) { isDragging ->
                    val rotation by animateFloatAsState(if (isDragging) DRAG_ROTATION_DEGREES else 0f)
                    val rowModifier = Modifier
                        .animateItem()
                        .graphicsLayer { rotationZ = rotation }
                        .let { base ->
                            if (state.isReorderEnabled) {
                                base.longPressDraggableHandle(
                                    onDragStopped = {
                                        val todos = listEntries.filterIsInstance<ListEntry.Todo>().map { it.item }
                                        // Put the ball back right away - dragging a todo just past
                                        // it doesn't change the todo order, so no state.items update
                                        // would come to re-sync it.
                                        listEntries = todos.withBall()
                                        onAction(TodoListAction.OnReorder(todos.map { it.id }))
                                    }
                                )
                            } else {
                                base
                            }
                        }
                    SwipeableTodoRow(
                        item = item,
                        cloudSegment = cloudSegments[item.id],
                        modifier = rowModifier,
                        // TalkBack's stand-in for dragging, offered exactly when dragging is.
                        moveActions = if (state.isReorderEnabled) {
                            moveAccessibilityActions(item.id, state.items.map { it.id }) { orderedIds ->
                                onAction(TodoListAction.OnReorder(orderedIds))
                            }
                        } else {
                            emptyList()
                        },
                        swipeEnabled = !isDragging,
                        onToggleDone = { onAction(TodoListAction.OnToggleDone(item.id)) },
                        onEditClick = { onAction(TodoListAction.OnEditTodoClick(item.id)) },
                        onDeleteClick = { onAction(TodoListAction.OnDeleteTodo(item.id)) },
                        onItemClick = {
                            detailItemId = item.id
                            onAction(TodoListAction.OnTodoDetailsOpened)
                        },
                        onShareSwipe = { shareItem = item },
                        // Only where long-press would otherwise do nothing - when reorder is on,
                        // the drag handle above owns long-press.
                        onLongClick = if (state.filter == TodoListFilter.Active && !state.isReorderEnabled) {
                            { onAction(TodoListAction.OnReorderUnavailable) }
                        } else {
                            null
                        }
                    )
                }
            }

            if (state.items.isEmpty()) {
                // The mascot fronts the empty state, both Active and Done.
                item(key = "empty_state") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(mascotDrawable(Res.drawable.mascot_sitting, Res.drawable.mascot_sitting_monochrome)),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(MASCOT_HEIGHT)
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.l))
                        Text(
                            text = when {
                                state.filter == TodoListFilter.Active -> stringResource(Res.string.todo_list_empty_active)
                                state.archiveCompletedTodos -> stringResource(Res.string.todo_list_empty_completed)
                                // Completing deletes, so this list only ever fills from the widget,
                                // which always archives - without this it just looks broken.
                                else -> stringResource(Res.string.todo_list_empty_completed_not_archived)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        val tip = state.emptyStateTip.takeIf { state.filter == TodoListFilter.Active }
                        if (state.filter == TodoListFilter.Done && !state.archiveCompletedTodos) {
                            Spacer(modifier = Modifier.height(AppSpacing.l))
                            FilledTonalButton(onClick = { onAction(TodoListAction.OnKeepCompletedTodosClick) }) {
                                Text(stringResource(Res.string.todo_list_keep_completed))
                            }
                        } else if (tip == EmptyStateTip.AddWidget || tip == EmptyStateTip.MoreSettings) {
                            Spacer(modifier = Modifier.height(AppSpacing.l))
                            EmptyStateTipText(
                                text = stringResource(
                                    if (tip == EmptyStateTip.AddWidget) {
                                        Res.string.todo_list_tip_widget
                                    } else {
                                        Res.string.todo_list_tip_more_settings
                                    }
                                )
                            )
                            if (tip == EmptyStateTip.AddWidget && state.canPinWidget) {
                                Spacer(modifier = Modifier.height(AppSpacing.m))
                                FilledTonalButton(onClick = { onAction(TodoListAction.OnAddWidgetClick) }) {
                                    Text(stringResource(Res.string.todo_list_add_widget))
                                }
                            }
                        }
                    }
                }
            } else {
                items(listEntries, key = { it.key }) { entry ->
                    when (entry) {
                        is ListEntry.Todo -> todoRow(entry.item)
                        // A ReorderableItem so a dragged todo swaps with it, but without a drag
                        // handle - the ball itself can't be picked up.
                        ListEntry.Ball -> ReorderableItem(reorderableListState, key = entry.key) {
                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacing.s)
                            ) {
                                Image(
                                    painter = painterResource(
                                        mascotDrawable(Res.drawable.mascot_ball, Res.drawable.mascot_ball_monochrome)
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(BiasAlignment(horizontalBias = ballHorizontalBias, verticalBias = 0f))
                                        .size(BALL_SIZE)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right above the + it's about, with the ball to draw the eye to it - not with the other
        // tips under the mascot, where it would be far from the button it explains.
        if (state.items.isEmpty() &&
            state.filter == TodoListFilter.Active &&
            state.emptyStateTip == EmptyStateTip.HoldAddButton
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(start = AppSpacing.l, end = AppSpacing.l, bottom = HOLD_ADD_TIP_BOTTOM_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(mascotDrawable(Res.drawable.mascot_ball, Res.drawable.mascot_ball_monochrome)),
                    contentDescription = null,
                    modifier = Modifier.size(BALL_SIZE)
                )
                Spacer(modifier = Modifier.height(AppSpacing.s))
                EmptyStateTipText(text = stringResource(Res.string.todo_list_tip_hold_add))
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier.align(actionAlignment)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(AppSpacing.l),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            AppFab(
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                icon = LocalAppIcons.current.scrollToTopIcon
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(AppSpacing.l)
        )
    }

    detailItemId?.let { id -> state.items.firstOrNull { it.id == id } }?.let { item ->
        TodoDetailDialog(
            item = item,
            onDismiss = { detailItemId = null },
            onToggleChecklistItem = { itemId -> onAction(TodoListAction.OnToggleChecklistItem(item.id, itemId)) },
            onCopied = {
                coroutineScope.launch { snackbarHostState.showSnackbar(getString(Res.string.snackbar_copied)) }
            }
        )
    }

    shareItem?.let { item ->
        val shareText = rememberShareText(item)
        TodoShareBottomSheet(
            shareText = shareText,
            onShareClick = {
                val launcher = nativeShareLauncher
                if (launcher != null) {
                    launcher(shareText)
                    onAction(TodoListAction.OnTodoShared(ShareMethod.Native))
                } else {
                    coroutineScope.launch {
                        clipboard.setClipEntry(createPlainTextClipEntry(shareText))
                        onAction(TodoListAction.OnTodoShared(ShareMethod.Copied))
                        snackbarHostState.showSnackbar(getString(Res.string.snackbar_copied))
                    }
                }
            },
            onDismiss = { shareItem = null }
        )
    }
}

// Quieter than the empty-state line above it - a hint, not the message.
@Composable
private fun EmptyStateTipText(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

private fun TodoSortOption.displayName(): StringResource = when (this) {
    TodoSortOption.Date -> Res.string.sort_date
    TodoSortOption.Manual -> Res.string.sort_manual
    TodoSortOption.Text -> Res.string.sort_text
}

@Composable
private fun CompletedSummaryCard(
    todayCount: Int,
    thisWeekCount: Int,
    thisMonthCount: Int,
    modifier: Modifier = Modifier
) {
    // Same color as the bottom navigation bar, so the card reads as part of the app chrome
    // rather than as another todo.
    AppCard(modifier = modifier, containerColor = ShortNavigationBarDefaults.containerColor) {
        // Extra padding on top of the card's own 8dp - 12dp total, same as the settings sections.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.xs)
        ) {
            Text(
                text = stringResource(Res.string.completed_summary_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(AppSpacing.s))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.completed_summary_today, todayCount),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(Res.string.completed_summary_this_week, thisWeekCount),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(Res.string.completed_summary_this_month, thisMonthCount),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Move to top / up / down / to bottom for one todo in the list as shown - each only where it'd
// actually move it. Hands back the whole new order, same as a finished drag.
@Composable
private fun moveAccessibilityActions(
    id: String,
    orderedIds: List<String>,
    onReorder: (List<String>) -> Unit
): List<CustomAccessibilityAction> {
    val index = orderedIds.indexOf(id)
    if (index == -1) return emptyList()
    val lastIndex = orderedIds.lastIndex
    return buildList {
        if (index > 0) {
            add(CustomAccessibilityAction(stringResource(Res.string.action_move_to_top)) { onReorder(orderedIds.moved(index, 0)); true })
            add(CustomAccessibilityAction(stringResource(Res.string.action_move_up)) { onReorder(orderedIds.moved(index, index - 1)); true })
        }
        if (index < lastIndex) {
            add(CustomAccessibilityAction(stringResource(Res.string.action_move_down)) { onReorder(orderedIds.moved(index, index + 1)); true })
            add(CustomAccessibilityAction(stringResource(Res.string.action_move_to_bottom)) { onReorder(orderedIds.moved(index, lastIndex)); true })
        }
    }
}

private fun List<String>.moved(from: Int, to: Int): List<String> = toMutableList().apply { add(to, removeAt(from)) }

@Composable
private fun SwipeableTodoRow(
    item: TodoItemUi,
    onToggleDone: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit,
    onShareSwipe: () -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    onLongClick: (() -> Unit)? = null,
    cloudSegment: CloudSegment? = null,
    moveActions: List<CustomAccessibilityAction> = emptyList()
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val revealedWidthPx = with(density) { (SWIPE_ACTION_WIDTH * 2).toPx() }

    // Checking an item off plays the strikethrough animation (TodoItemCardContent) first, then
    // fires the real toggle once it's done - the item's actual removal from the list (once its
    // status flips and it's filtered out of state.items) is handled by animateItem() below, so
    // the two animations chain: strike, then collapse-out.
    var isCompleting by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(isCompleting) {
        if (isCompleting) {
            delay(STRIKETHROUGH_ANIMATION_DURATION_MS.toLong())
            onToggleDone()
        }
    }

    val swipeState = remember(revealedWidthPx) {
        AnchoredDraggableState(
            initialValue = SwipeAnchor.Closed,
            anchors = DraggableAnchors {
                SwipeAnchor.ShareTrigger at revealedWidthPx
                SwipeAnchor.Closed at 0f
                SwipeAnchor.Open at -revealedWidthPx
            }
        )
    }

    fun closeSwipe() {
        coroutineScope.launch { swipeState.animateTo(SwipeAnchor.Closed) }
    }

    // Reordering (drag) and swipe-to-reveal both react to horizontal drags on the same row and
    // fight each other if left open together - snap any revealed actions closed as soon as a
    // drag disables swipe.
    LaunchedEffect(swipeEnabled) {
        if (!swipeEnabled) closeSwipe()
    }

    // A right swipe is a trigger, not a resting state - once it settles there, fire the
    // callback and snap straight back to closed instead of staying open like the left swipe.
    LaunchedEffect(swipeState.settledValue) {
        if (swipeState.settledValue == SwipeAnchor.ShareTrigger) {
            onShareSwipe()
            swipeState.animateTo(SwipeAnchor.Closed)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = AppSpacing.s)
                .background(MaterialTheme.colorScheme.surfaceVariant, appListItemCardShape())
                // Hidden under the card until swiped open - TalkBack would otherwise stop on them
                // with no word of which todo they belong to. The card's own actions cover them.
                .then(
                    if (swipeState.currentValue == SwipeAnchor.Closed) Modifier.clearAndSetSemantics {} else Modifier
                ),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onEditClick(); closeSwipe() },
                modifier = Modifier.size(SWIPE_ACTION_WIDTH)
            ) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(Res.string.action_edit))
            }
            IconButton(
                onClick = { onDeleteClick() },
                modifier = Modifier.size(SWIPE_ACTION_WIDTH)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Res.string.action_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        TodoItemCard(
            text = item.text,
            isDone = item.status == TodoStatus.Done || isCompleting,
            formattedDate = rememberShortDateFormat().format(item.creationDate),
            attention = item.attention,
            tags = item.tags,
            checklistDone = item.checklist.count { it.isDone },
            checklistTotal = item.checklist.size,
            nextReminder = item.nextReminder,
            cloudSegment = cloudSegment,
            onToggleDone = {
                if (item.status == TodoStatus.Active) {
                    isCompleting = true
                } else {
                    onToggleDone()
                }
            },
            onClick = onItemClick,
            onLongClick = onLongClick,
            // Every swipe action, plus moving, without the gestures TalkBack can't do.
            customActions = listOf(
                CustomAccessibilityAction(stringResource(Res.string.action_edit)) { onEditClick(); true },
                CustomAccessibilityAction(stringResource(Res.string.action_delete)) { onDeleteClick(); true },
                CustomAccessibilityAction(stringResource(Res.string.share_title)) { onShareSwipe(); true }
            ) + moveActions,
            modifier = Modifier
                .padding(horizontal = AppSpacing.s)
                .offset { IntOffset(x = swipeState.requireOffset().roundToInt(), y = 0) }
                .anchoredDraggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    enabled = swipeEnabled
                )
        )
    }
}

@Preview
@Composable
private fun TodoListScreenPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoListScreen(
            state = TodoListState(
                items = listOf(
                    TodoItemUi(id = "1", text = "Buy groceries", status = TodoStatus.Active, sortOrder = 0, creationDate = LocalDate(2026, 8, 24)),
                    TodoItemUi(id = "2", text = "Write project architecture document covering module boundaries, data flow, and testing strategy for the new feature", status = TodoStatus.Done, sortOrder = 1, creationDate = LocalDate(2026, 8, 23))
                )
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun TodoListScreenManualSortPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoListScreen(
            state = TodoListState(
                items = listOf(
                    TodoItemUi(id = "1", text = "Buy groceries", status = TodoStatus.Active, sortOrder = 0, creationDate = LocalDate(2026, 8, 24)),
                    TodoItemUi(id = "2", text = "Write project architecture document", status = TodoStatus.Active, sortOrder = 1, creationDate = LocalDate(2026, 8, 23))
                ),
                sortOption = TodoSortOption.Manual
            ),
            onAction = {}
        )
    }
}
