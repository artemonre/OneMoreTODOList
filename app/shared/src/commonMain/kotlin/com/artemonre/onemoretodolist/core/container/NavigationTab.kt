package com.artemonre.onemoretodolist.core.container

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import org.jetbrains.compose.resources.StringResource

// A feature contributes one of these per tab it wants in the bottom navigation bar; the
// composition root (App) decides which tabs exist and in what order.
data class NavigationTab(
    val label: StringResource,
    val icon: ImageVector,
    val startDestination: NavKey,
    val entries: EntryProviderScope<NavKey>.() -> Unit,
    // Registers every NavKey subtype this tab's screens use, so the back stack can be saved and
    // restored across process death - one subclass(...) per route.
    val registerRoutes: PolymorphicModuleBuilder<NavKey>.() -> Unit,
    // The docked FAB shown over the nav bar while this tab is selected, or none if the tab
    // doesn't need one. Resolves its own ViewModel (koinViewModel() is store-owner-scoped, not
    // entry-scoped here - see TodoListNavigation.todoListTab()), so it stays in sync with the
    // tab's own screen even though the container renders it.
    val fab: (@Composable () -> Unit)? = null
)
