package com.artemonre.onemoretodolist.feature.settings.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.feature.settings.presentation.SettingsRoot

fun EntryProviderScope<NavKey>.settingsEntries() {
    entry(SettingsRoute.Main) {
        SettingsRoot()
    }
}

// Settings is a whole-app concern the container always appends as the last tab,
// regardless of which gateway/feature modules are present.
fun settingsTab(): NavigationTab = NavigationTab(
    label = "Settings",
    icon = Icons.Filled.Settings,
    startDestination = SettingsRoute.Main,
    entries = { settingsEntries() }
)
