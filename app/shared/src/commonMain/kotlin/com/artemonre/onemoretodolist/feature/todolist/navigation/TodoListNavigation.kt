package com.artemonre.onemoretodolist.feature.todolist.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.core.designsystem.components.AppFabMenu
import com.artemonre.onemoretodolist.core.designsystem.components.FabMenuItem
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListAction
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListRoot
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListViewModel
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.todoListEntries() {
    entry(TodoListRoute.List) {
        TodoListRoot()
    }
}

@Composable
private fun TodoListFab(viewModel: TodoListViewModel = koinViewModel()) {
    var menuExpanded by remember { mutableStateOf(false) }
    val addIcon = LocalAppIcons.current.addIcon

    AppFabMenu(
        expanded = menuExpanded,
        onExpandedChange = { menuExpanded = it },
        onClick = { viewModel.onAction(TodoListAction.OnAddTodoClick) },
        icon = addIcon,
        items = listOf(
            // Screen-dependent. Unlike the plain tap (which still opens the bottom sheet, as
            // before), this menu entry opens the full-screen add flow - a more detailed creation
            // flow may replace it there later.
            FabMenuItem(
                label = "Create a todo",
                icon = addIcon,
                onClick = { viewModel.onAction(TodoListAction.OnAddTodoFullScreenClick) }
            ),
            // Common, not screen-specific - no action wired up yet.
            FabMenuItem(
                label = "Capture a note",
                icon = Icons.Filled.Create,
                enabled = false,
                onClick = {}
            )
        )
    )
}

fun todoListTab(): NavigationTab = NavigationTab(
    label = "Todo",
    icon = Icons.AutoMirrored.Filled.List,
    startDestination = TodoListRoute.List,
    entries = { todoListEntries() },
    fab = { TodoListFab() }
)
