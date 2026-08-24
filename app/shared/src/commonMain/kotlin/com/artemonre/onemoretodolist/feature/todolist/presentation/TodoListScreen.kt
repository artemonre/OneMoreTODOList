package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TodoListRoot(
    viewModel: TodoListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    TodoListScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun TodoListScreen(
    state: TodoListState,
    onAction: (TodoListAction) -> Unit
) {
    if (state.items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No todo items yet")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(state.items, key = { it.id }) { item ->
            ListItem(
                modifier = Modifier.padding(horizontal = 8.dp),
                headlineContent = { Text(item.title) },
                supportingContent = { Text(item.formattedDate) },
                leadingContent = {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { onAction(TodoListAction.OnToggleDone(item.id)) }
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun TodoListScreenPreview() {
    AppTheme {
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
