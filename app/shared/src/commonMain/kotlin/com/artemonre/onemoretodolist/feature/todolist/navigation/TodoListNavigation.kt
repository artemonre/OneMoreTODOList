package com.artemonre.onemoretodolist.feature.todolist.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.core.designsystem.components.AppFab
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
    AppFab(
        onClick = { viewModel.onAction(TodoListAction.OnAddTodoClick) },
        icon = LocalAppIcons.current.addIcon
    )
}

fun todoListTab(): NavigationTab = NavigationTab(
    label = "Todo",
    icon = Icons.AutoMirrored.Filled.List,
    startDestination = TodoListRoute.List,
    entries = { todoListEntries() },
    fab = { TodoListFab() }
)
