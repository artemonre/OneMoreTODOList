package com.artemonre.onemoretodolist.core.container

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List as ListIcon
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.artemonre.onemoretodolist.core.designsystem.components.AppNavigationBar
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.settings.navigation.SettingsRoute
import com.artemonre.onemoretodolist.feature.todolist.navigation.TodoListRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// Every feature's NavKey subtypes must be registered here so the back stack can be
// saved/restored across process death. Add a `subclass(...)` line per new route as
// features are added.
private val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(TodoListRoute.List::class)
            subclass(SettingsRoute.Main::class)
        }
    }
}

@Composable
fun ContainerRoot(
    contentTabs: List<NavigationTab>,
    viewModel: ContainerViewModel = koinViewModel(parameters = { parametersOf(contentTabs) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(ContainerAction.OnStart)
    }

    ContainerScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun ContainerScreen(
    state: ContainerState,
    onAction: (ContainerAction) -> Unit
) {
    val backStack = rememberNavBackStack(
        navSavedStateConfiguration,
        state.tabs.getOrNull(state.selectedTabIndex)?.startDestination ?: TodoListRoute.List
    )

    LaunchedEffect(state.selectedTabIndex, state.tabs) {
        val startDestination = state.tabs.getOrNull(state.selectedTabIndex)?.startDestination ?: return@LaunchedEffect
        if (backStack.lastOrNull() != startDestination) {
            backStack.clear()
            backStack.add(startDestination)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AppNavigationBar(
                items = state.tabs,
                selectedIndex = state.selectedTabIndex,
                onItemSelected = { onAction(ContainerAction.OnTabSelected(it)) },
                icon = { it.icon },
                label = { it.label }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                state.tabs.forEach { tab -> tab.entries(this) }
            }
        )
    }
}

@Serializable
private data object ContainerPreviewRoute : NavKey

@Preview
@Composable
private fun ContainerScreenPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        ContainerScreen(
            state = ContainerState(
                tabs = listOf(
                    NavigationTab(
                        label = "Todo",
                        icon = Icons.AutoMirrored.Filled.ListIcon,
                        startDestination = ContainerPreviewRoute,
                        entries = { entry(ContainerPreviewRoute) { } }
                    ),
                    NavigationTab(
                        label = "Settings",
                        icon = Icons.Filled.Settings,
                        startDestination = ContainerPreviewRoute,
                        entries = { entry(ContainerPreviewRoute) { } }
                    )
                ),
                selectedTabIndex = 0
            ),
            onAction = {}
        )
    }
}
