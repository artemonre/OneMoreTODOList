package com.artemonre.onemoretodolist.core.container

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

// A gateway (or, later, an extra feature module) contributes one of these per tab it wants
// in the bottom navigation bar. The container always appends its own Settings tab last.
data class NavigationTab(
    val label: String,
    val icon: ImageVector,
    val startDestination: NavKey,
    val entries: EntryProviderScope<NavKey>.() -> Unit
)
