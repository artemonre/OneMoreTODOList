package com.artemonre.onemoretodolist.feature.todolist.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListRoot

fun EntryProviderScope<NavKey>.todoListEntries() {
    entry(TodoListRoute.List) {
        TodoListRoot()
    }
}

fun todoListTab(): NavigationTab = NavigationTab(
    label = "Todo",
    icon = Icons.AutoMirrored.Filled.List,
    startDestination = TodoListRoute.List,
    entries = { todoListEntries() }
)
