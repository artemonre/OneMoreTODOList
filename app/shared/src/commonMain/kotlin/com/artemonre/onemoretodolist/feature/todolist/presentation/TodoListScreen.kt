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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.designsystem.components.AppCard
import com.artemonre.onemoretodolist.core.designsystem.components.AppChipGroup
import com.artemonre.onemoretodolist.core.designsystem.components.AppFab
import com.artemonre.onemoretodolist.core.designsystem.components.appListItemCardShape
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalActionPlacement
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.core.presentation.ObserveAsEvents
import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.createPlainTextClipEntry
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.rememberNativeShareLauncher
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.action_delete
import onemoretodolist.app.shared.generated.resources.action_edit
import onemoretodolist.app.shared.generated.resources.action_undo
import onemoretodolist.app.shared.generated.resources.completed_summary_this_month
import onemoretodolist.app.shared.generated.resources.completed_summary_this_week
import onemoretodolist.app.shared.generated.resources.completed_summary_title
import onemoretodolist.app.shared.generated.resources.completed_summary_today
import onemoretodolist.app.shared.generated.resources.mascot_staying
import onemoretodolist.app.shared.generated.resources.mascot_staying_green
import onemoretodolist.app.shared.generated.resources.snackbar_copied
import onemoretodolist.app.shared.generated.resources.sort_date
import onemoretodolist.app.shared.generated.resources.sort_manual
import onemoretodolist.app.shared.generated.resources.sort_text
import onemoretodolist.app.shared.generated.resources.todo_filter_active
import onemoretodolist.app.shared.generated.resources.todo_filter_done
import onemoretodolist.app.shared.generated.resources.todo_list_empty_active
import onemoretodolist.app.shared.generated.resources.todo_list_empty_completed
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
private const val MASCOT_AFTER_TODO_COUNT = 5

private enum class SwipeAnchor { Closed, Open, ShareTrigger }

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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TodoListEvent.ShowAddTodoSheet -> showAddTodoSheet = true
            TodoListEvent.ShowAddTodoFullScreenDialog -> {
                addTodoDraftText = ""
                showAddTodoFullScreenDialog = true
            }
            is TodoListEvent.ShowEditTodoSheet -> editingTodo = event.item
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
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(draft))
                showAddTodoSheet = false
            },
            onDismiss = { showAddTodoSheet = false },
            onMoreSettingsClick = { draftText ->
                showAddTodoSheet = false
                addTodoDraftText = draftText
                showAddTodoFullScreenDialog = true
            }
        )
    }

    if (showAddTodoFullScreenDialog) {
        TodoFormFullScreenDialog(
            editingItem = null,
            onConfirm = { draft ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(draft))
                showAddTodoFullScreenDialog = false
            },
            onDismiss = { showAddTodoFullScreenDialog = false },
            initialText = addTodoDraftText,
            knownTags = state.knownTags
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
    var manualOrderItems by remember { mutableStateOf(state.items) }
    LaunchedEffect(state.items) { manualOrderItems = state.items }

    // from.index/to.index are positions in the whole LazyColumn, not in manualOrderItems - the
    // summary card, filter/sort header and mascot items shift them. Look items up by key instead of trusting the
    // raw index.
    val reorderableListState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = manualOrderItems.indexOfFirst { it.id == from.key }
        val toIndex = manualOrderItems.indexOfFirst { it.id == to.key }
        if (fromIndex != -1 && toIndex != -1) {
            manualOrderItems = manualOrderItems.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        }
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
                            Button(
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
                            if (state.filter == TodoListFilter.Active && state.sortOption == TodoSortOption.Manual) {
                                base.longPressDraggableHandle(
                                    onDragStopped = {
                                        onAction(TodoListAction.OnReorder(manualOrderItems.map { it.id }))
                                    }
                                )
                            } else {
                                base
                            }
                        }
                    SwipeableTodoRow(
                        item = item,
                        modifier = rowModifier,
                        swipeEnabled = !isDragging,
                        onToggleDone = { onAction(TodoListAction.OnToggleDone(item.id)) },
                        onEditClick = { onAction(TodoListAction.OnEditTodoClick(item.id)) },
                        onDeleteClick = { onAction(TodoListAction.OnDeleteTodo(item.id)) },
                        onItemClick = { detailItemId = item.id },
                        onShareSwipe = { shareItem = item }
                    )
                }
            }

            if (state.items.isEmpty()) {
                // The green mascot fronts the empty state (both Active and Done) in place of the
                // blue one that sits among the todos otherwise.
                item(key = "empty_state") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.mascot_staying_green),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(MASCOT_HEIGHT)
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.l))
                        Text(
                            text = if (state.filter == TodoListFilter.Done) {
                                stringResource(Res.string.todo_list_empty_completed)
                            } else {
                                stringResource(Res.string.todo_list_empty_active)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(manualOrderItems.take(MASCOT_AFTER_TODO_COUNT), key = { it.id }) { todoRow(it) }

                // Sits after the first MASCOT_AFTER_TODO_COUNT todos, or after all of them when
                // there are fewer. Not a ReorderableItem, so dragging a todo across it is a no-op -
                // the reorder callback above looks items up by key and ignores anything outside
                // manualOrderItems.
                item(key = "mascot") {
                    Image(
                        painter = painterResource(Res.drawable.mascot_staying),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MASCOT_HEIGHT)
                    )
                }

                items(manualOrderItems.drop(MASCOT_AFTER_TODO_COUNT), key = { it.id }) { todoRow(it) }
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
        TodoShareBottomSheet(
            itemText = item.text,
            onShareClick = {
                val launcher = nativeShareLauncher
                if (launcher != null) {
                    launcher(item.text)
                } else {
                    coroutineScope.launch {
                        clipboard.setClipEntry(createPlainTextClipEntry(item.text))
                        snackbarHostState.showSnackbar(getString(Res.string.snackbar_copied))
                    }
                }
            },
            onDismiss = { shareItem = null }
        )
    }
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
                style = MaterialTheme.typography.titleMedium
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

@Composable
private fun SwipeableTodoRow(
    item: TodoItemUi,
    onToggleDone: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit,
    onShareSwipe: () -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true
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
                .background(MaterialTheme.colorScheme.surfaceVariant, appListItemCardShape()),
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
            id = item.id,
            text = item.text,
            isDone = item.status == TodoStatus.Done || isCompleting,
            formattedDate = rememberShortDateFormat().format(item.creationDate),
            attention = item.attention,
            tags = item.tags,
            checklistDone = item.checklist.count { it.isDone },
            checklistTotal = item.checklist.size,
            onToggleDone = {
                if (item.status == TodoStatus.Active) {
                    isCompleting = true
                } else {
                    onToggleDone()
                }
            },
            onClick = onItemClick,
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
