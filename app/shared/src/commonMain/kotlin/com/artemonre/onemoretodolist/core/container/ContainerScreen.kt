package com.artemonre.onemoretodolist.core.container

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List as ListIcon
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

// Half of Material3's default FAB size (56dp, FabBaselineTokens.ContainerHeight) - shifting the
// docked FAB up by this much centers it on the nav bar's top edge, so it straddles the bar
// instead of sitting flush inside or fully above it.
private val FAB_NAV_BAR_OVERLAP = 28.dp

// The outgoing screen exits to the left while the incoming one enters from the right - used when
// switching to a tab positioned to the right of the current one.
private fun tabSlideLeft(): ContentTransform =
    slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })

// The outgoing screen exits to the right while the incoming one enters from the left - used when
// switching to a tab positioned to the left of the current one.
private fun tabSlideRight(): ContentTransform =
    slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })

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

    // A tab switch replaces the back stack wholesale (see below) rather than pushing/popping it,
    // so NavDisplay can't infer a direction from the stack shape alone - it's set here instead,
    // synchronously in the nav bar's click handler where the old and new index are both known
    // before state.selectedTabIndex actually changes. (Deriving it reactively from
    // state.selectedTabIndex doesn't work: the back stack mutation below happens a frame later,
    // inside LaunchedEffect, by which point a naive "previous index" would already have caught up
    // to the new one.)
    var isTabMovingRight by remember { mutableStateOf(true) }

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
            Box(contentAlignment = Alignment.TopCenter) {
                AppNavigationBar(
                    items = state.tabs,
                    selectedIndex = state.selectedTabIndex,
                    onItemSelected = { index ->
                        isTabMovingRight = index >= state.selectedTabIndex
                        onAction(ContainerAction.OnTabSelected(index))
                    },
                    icon = { it.icon },
                    label = { it.label }
                )
                state.tabs.getOrNull(state.selectedTabIndex)?.fab?.let { fab ->
                    Box(modifier = Modifier.offset(y = -FAB_NAV_BAR_OVERLAP)) {
                        fab()
                    }
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                state.tabs.forEach { tab -> tab.entries(this) }
            },
            // A tab switch is a backStack.clear() + add() (see the LaunchedEffect above), which
            // NavDisplay treats as a pop rather than a push - so the pop/predictive-pop specs are
            // the ones actually exercised here. transitionSpec is still set to match, in case
            // that ever changes.
            transitionSpec = { if (isTabMovingRight) tabSlideLeft() else tabSlideRight() },
            popTransitionSpec = { if (isTabMovingRight) tabSlideLeft() else tabSlideRight() },
            predictivePopTransitionSpec = { if (isTabMovingRight) tabSlideLeft() else tabSlideRight() }
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
