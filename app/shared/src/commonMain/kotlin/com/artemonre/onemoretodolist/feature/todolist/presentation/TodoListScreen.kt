package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalActionPlacement
import com.artemonre.onemoretodolist.core.presentation.ObserveAsEvents
import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val SCROLL_TO_TOP_THRESHOLD = 10

@Composable
fun TodoListRoot(
    viewModel: TodoListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddTodoSheet by remember { mutableStateOf(false) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            TodoListEvent.ShowAddTodoSheet -> showAddTodoSheet = true
        }
    }

    TodoListScreen(
        state = state,
        onAction = viewModel::onAction
    )

    if (showAddTodoSheet) {
        AddTodoBottomSheet(
            onConfirm = { title, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(title, isPrioritized))
                showAddTodoSheet = false
            },
            onDismiss = { showAddTodoSheet = false }
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
                    .only(WindowInsetsSides.Vertical)
                    .add(WindowInsets(bottom = 8.dp))
                    .asPaddingValues(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    ElevatedCard(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(item.title) },
                            supportingContent = { Text(item.formattedDate) },
                            leadingContent = {
                                AppCheckToggle(
                                    checked = item.isDone,
                                    onCheckedChange = { onAction(TodoListAction.OnToggleDone(item.id)) }
                                )
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !showScrollToTop,
            modifier = Modifier.align(actionAlignment).safeDrawingPadding().padding(16.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(onClick = { onAction(TodoListAction.OnAddTodoClick) }) {
                Icon(imageVector = LocalAppIcons.current.addIcon, contentDescription = null)
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier.align(actionAlignment).safeDrawingPadding().padding(16.dp),
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

@Preview
@Composable
private fun TodoListScreenPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoListScreen(
            state = TodoListState(
                items = listOf(
                    TodoItemUi(id = "1", title = "Buy groceries", isDone = false, sortOrder = 0, formattedDate = "Aug 24, 2026"),
                    TodoItemUi(id = "2", title = "Write project architecture", isDone = true, sortOrder = 1, formattedDate = "Aug 23, 2026")
                )
            ),
            onAction = {}
        )
    }
}
