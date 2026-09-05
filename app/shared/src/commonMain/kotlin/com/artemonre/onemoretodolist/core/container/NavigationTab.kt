package com.artemonre.onemoretodolist.core.container

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

// A gateway (or, later, an extra feature module) contributes one of these per tab it wants
// in the bottom navigation bar. The container always appends its own Settings tab last.
data class NavigationTab(
    val label: String,
    val icon: ImageVector,
    val startDestination: NavKey,
    val entries: EntryProviderScope<NavKey>.() -> Unit,
    // The docked FAB shown over the nav bar while this tab is selected, or none if the tab
    // doesn't need one. Resolves its own ViewModel (koinViewModel() is store-owner-scoped, not
    // entry-scoped here - see TodoListNavigation.todoListTab()), so it stays in sync with the
    // tab's own screen even though the container renders it.
    val fab: (@Composable () -> Unit)? = null
)
