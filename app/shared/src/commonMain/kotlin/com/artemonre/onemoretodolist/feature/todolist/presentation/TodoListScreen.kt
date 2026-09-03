package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.artemonre.onemoretodolist.createPlainTextClipEntry
import com.artemonre.onemoretodolist.rememberNativeShareLauncher
import com.artemonre.onemoretodolist.core.designsystem.components.AppFab
import com.artemonre.onemoretodolist.core.designsystem.components.appListItemCardShape
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalActionPlacement
import com.artemonre.onemoretodolist.core.presentation.ObserveAsEvents
import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private const val SCROLL_TO_TOP_THRESHOLD = 10
private const val DRAG_ROTATION_DEGREES = 4f
private val SWIPE_ACTION_WIDTH = 56.dp

// Clears the scroll-to-top button (56dp) plus its 16dp margin, with a little extra breathing
// room, so the last list item never ends up hidden behind it after a full scroll.
private val LIST_BOTTOM_CONTENT_PADDING = 80.dp

private enum class SwipeAnchor { Closed, Open, ShareTrigger }

@Composable
fun TodoListRoot(
    viewModel: TodoListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingTodo by remember { mutableStateOf<TodoItemUi?>(null) }
    var showAddTodoSheet by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TodoListEvent.ShowAddTodoSheet -> showAddTodoSheet = true
            is TodoListEvent.ShowEditTodoSheet -> editingTodo = event.item
        }
    }

    TodoListScreen(
        state = state,
        onAction = viewModel::onAction
    )

    if (showAddTodoSheet) {
        TodoFormBottomSheet(
            editingItem = null,
            onConfirm = { text, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(text, isPrioritized))
                showAddTodoSheet = false
            },
            onDismiss = { showAddTodoSheet = false }
        )
    }

    editingTodo?.let { item ->
        TodoFormBottomSheet(
            editingItem = item,
            onConfirm = { text, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmEditTodo(item.id, text, isPrioritized))
                editingTodo = null
            },
            onDismiss = { editingTodo = null }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TodoListScreen(
    state: TodoListState,
    onAction: (TodoListAction) -> Unit
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
    var detailItem by remember { mutableStateOf<TodoItemUi?>(null) }
    var shareItem by remember { mutableStateOf<TodoItemUi?>(null) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val nativeShareLauncher = rememberNativeShareLauncher()
    val clipboard = LocalClipboard.current

    // The reorderable library needs a local mutable list it can shuffle live during a drag.
    // It's re-synced from state.items whenever that changes - which OnReorder itself never
    // triggers mid-drag (it's only dispatched on drag-stop), so this can't fight an in-progress
    // drag; it only picks up genuinely external changes (add/edit/delete/toggle elsewhere).
    var manualOrderItems by remember { mutableStateOf(state.items) }
    LaunchedEffect(state.items) { manualOrderItems = state.items }

    val reorderableListState = rememberReorderableLazyListState(listState) { from, to ->
        manualOrderItems = manualOrderItems.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.safeDrawing
                .only(WindowInsetsSides.Top)
                .add(WindowInsets(top = 8.dp, bottom = LIST_BOTTOM_CONTENT_PADDING))
                .asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sort",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box {
                        Button(onClick = { sortMenuExpanded = true }) {
                            Text(state.sortOption.displayName())
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            TodoSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName()) },
                                    onClick = {
                                        onAction(TodoListAction.OnSortOptionSelected(option))
                                        sortMenuExpanded = false
                                    },
                                    leadingIcon = if (option == state.sortOption) {
                                        { Icon(imageVector = Icons.Filled.Check, contentDescription = null) }
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (state.items.isEmpty()) {
                item {
                    Text(
                        text = "No todo items yet",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(manualOrderItems, key = { it.id }) { item ->
                    ReorderableItem(reorderableListState, key = item.id) { isDragging ->
                        val rotation by animateFloatAsState(if (isDragging) DRAG_ROTATION_DEGREES else 0f)
                        val rowModifier = Modifier
                            .animateItem()
                            .graphicsLayer { rotationZ = rotation }
                            .let { base ->
                                if (state.sortOption == TodoSortOption.Manual) {
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
                            onToggleDone = { onAction(TodoListAction.OnToggleDone(item.id)) },
                            onEditClick = { onAction(TodoListAction.OnEditTodoClick(item.id)) },
                            onDeleteClick = { onAction(TodoListAction.OnDeleteTodo(item.id)) },
                            onItemClick = { detailItem = item },
                            onShareSwipe = { shareItem = item }
                        )
                    }
                }
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier.align(actionAlignment)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(16.dp),
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
                .padding(16.dp)
        )
    }

    detailItem?.let { item ->
        TodoDetailDialog(
            item = item,
            onDismiss = { detailItem = null },
            onCopied = {
                coroutineScope.launch { snackbarHostState.showSnackbar("Copied to clipboard") }
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
                        snackbarHostState.showSnackbar("Copied to clipboard")
                    }
                }
            },
            onDismiss = { shareItem = null }
        )
    }
}

private fun TodoSortOption.displayName(): String = when (this) {
    TodoSortOption.Date -> "Default"
    TodoSortOption.Manual -> "Manual"
    TodoSortOption.Text -> "Text"
}

@Composable
private fun SwipeableTodoRow(
    item: TodoItemUi,
    onToggleDone: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit,
    onShareSwipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val revealedWidthPx = with(density) { (SWIPE_ACTION_WIDTH * 2).toPx() }

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
                .padding(horizontal = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, appListItemCardShape()),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onEditClick(); closeSwipe() },
                modifier = Modifier.size(SWIPE_ACTION_WIDTH)
            ) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(
                onClick = { onDeleteClick() },
                modifier = Modifier.size(SWIPE_ACTION_WIDTH)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        TodoItemCard(
            text = item.text,
            isDone = item.status == TodoStatus.Done,
            formattedDate = item.formattedDate,
            onToggleDone = onToggleDone,
            onClick = onItemClick,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .offset { IntOffset(x = swipeState.requireOffset().roundToInt(), y = 0) }
                .anchoredDraggable(state = swipeState, orientation = Orientation.Horizontal)
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
                    TodoItemUi(id = "1", text = "Buy groceries", status = TodoStatus.Active, sortOrder = 0, formattedDate = "Aug 24, 2026"),
                    TodoItemUi(id = "2", text = "Write project architecture document covering module boundaries, data flow, and testing strategy for the new feature", status = TodoStatus.Done, sortOrder = 1, formattedDate = "Aug 23, 2026")
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
                    TodoItemUi(id = "1", text = "Buy groceries", status = TodoStatus.Active, sortOrder = 0, formattedDate = "Aug 24, 2026"),
                    TodoItemUi(id = "2", text = "Write project architecture document", status = TodoStatus.Active, sortOrder = 1, formattedDate = "Aug 23, 2026")
                ),
                sortOption = TodoSortOption.Manual
            ),
            onAction = {}
        )
    }
}
