package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalActionPlacement
import com.artemonre.onemoretodolist.core.presentation.ObserveAsEvents
import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val SCROLL_TO_TOP_THRESHOLD = 10
private val SWIPE_ACTION_WIDTH = 56.dp

private enum class SwipeAnchor { Closed, Open }

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
            onConfirm = { title, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(title, isPrioritized))
                showAddTodoSheet = false
            },
            onDismiss = { showAddTodoSheet = false }
        )
    }

    editingTodo?.let { item ->
        TodoFormBottomSheet(
            editingItem = item,
            onConfirm = { title, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmEditTodo(item.id, title, isPrioritized))
                editingTodo = null
            },
            onDismiss = { editingTodo = null }
        )
    }
}

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

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.items.isEmpty()) {
            Text(
                text = "No todo items yet",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Top)
                    .add(WindowInsets(top = 8.dp, bottom = 8.dp))
                    .asPaddingValues(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    SwipeableTodoRow(
                        item = item,
                        modifier = Modifier.animateItem(),
                        onToggleDone = { onAction(TodoListAction.OnToggleDone(item.id)) },
                        onEditClick = { onAction(TodoListAction.OnEditTodoClick(item.id)) },
                        onDeleteClick = { onAction(TodoListAction.OnDeleteTodo(item.id)) }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !showScrollToTop,
            modifier = Modifier.align(actionAlignment)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(16.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(onClick = { onAction(TodoListAction.OnAddTodoClick) }) {
                Icon(imageVector = LocalAppIcons.current.addIcon, contentDescription = null)
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier.align(actionAlignment)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(16.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } }
            ) {
                Icon(imageVector = LocalAppIcons.current.scrollToTopIcon, contentDescription = null)
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
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val revealedWidthPx = with(density) { (SWIPE_ACTION_WIDTH * 2).toPx() }

    val swipeState = remember(revealedWidthPx) {
        AnchoredDraggableState(
            initialValue = SwipeAnchor.Closed,
            anchors = DraggableAnchors {
                SwipeAnchor.Closed at 0f
                SwipeAnchor.Open at -revealedWidthPx
            }
        )
    }

    fun closeSwipe() {
        coroutineScope.launch { swipeState.animateTo(SwipeAnchor.Closed) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
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

        ElevatedCard(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .offset { IntOffset(x = swipeState.requireOffset().roundToInt(), y = 0) }
                .anchoredDraggable(state = swipeState, orientation = Orientation.Horizontal),
            shape = RoundedCornerShape(4.dp)
        ) {
            val titleStyle = MaterialTheme.typography.bodyLarge
            val dateStyle = MaterialTheme.typography.labelSmall
            val titleRowHeight = with(density) { titleStyle.lineHeight.toDp() * 2 }
            val dateHeight = with(density) { dateStyle.lineHeight.toDp() }
            val dateSpacing = 4.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .height(titleRowHeight + dateSpacing + dateHeight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppCheckToggle(
                        checked = item.status == TodoStatus.Done,
                        onCheckedChange = { onToggleDone() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.title,
                        style = titleStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (item.status == TodoStatus.Done) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = item.formattedDate,
                    style = dateStyle,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun TodoListScreenPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoListScreen(
            state = TodoListState(
                items = listOf(
                    TodoItemUi(id = "1", title = "Buy groceries", status = TodoStatus.Active, sortOrder = 0, formattedDate = "Aug 24, 2026"),
                    TodoItemUi(id = "2", title = "Write project architecture document covering module boundaries, data flow, and testing strategy for the new feature", status = TodoStatus.Done, sortOrder = 1, formattedDate = "Aug 23, 2026")
                )
            ),
            onAction = {}
        )
    }
}
