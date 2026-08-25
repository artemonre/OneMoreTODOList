package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.data.themeModule
import com.artemonre.onemoretodolist.feature.todolist.di.todoListModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.TodoListRoute
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListEntries
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration

// Every feature's NavKey subtypes must be registered here so the back stack can be
// saved/restored across process death. Add a `subclass(...)` line per new route as
// features are added.
private val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(TodoListRoute.List::class)
        }
    }
}

@Composable
fun App(platformModules: List<Module>) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(todoListModule, themeModule, *platformModules.toTypedArray())
        }
    ) {
        AppTheme {
            val backStack = rememberNavBackStack(navSavedStateConfiguration, TodoListRoute.List)
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider { todoListEntries() }
            )
        }
    }
}
