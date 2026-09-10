package com.artemonre.onemoretodolist.feature.todolist.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalClipboard
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.core.designsystem.components.AppFabMenu
import com.artemonre.onemoretodolist.core.designsystem.components.FabMenuItem
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.createPlainTextClipEntry
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoFormBottomSheet
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoFormFullScreenDialog
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListAction
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListRoot
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListViewModel
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoShareBottomSheet
import com.artemonre.onemoretodolist.rememberNativeShareLauncher
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.todoListEntries() {
    entry(TodoListRoute.List) {
        TodoListRoot()
    }
}

@Composable
private fun TodoListFab(viewModel: TodoListViewModel = koinViewModel()) {
    val overlayState by viewModel.overlayState.collectAsStateWithLifecycle()
    // Hidden while any todo overlay (add/edit/share sheet, full-screen add dialog) is open - it
    // would otherwise float on top of TodoListOverlay's scrim (see todoListTab()'s overlay slot).
    if (overlayState.isActive) return

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
                icon = Icons.Filled.PhotoCamera,
                enabled = false,
                onClick = {}
            )
        )
    )
}

// Renders whichever todo sheet/dialog is currently open (see TodoListViewModel.overlayState).
// Hosted by the container above the nav bar and FAB (see todoListTab()'s overlay slot) rather
// than nested inside TodoListRoot's own content, so it can cover the whole screen. Resolves the
// same shared ViewModel instance as TodoListRoot and TodoListFab - see NavigationTab.fab's doc
// comment for why koinViewModel() works here despite being called outside the nav entry.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TodoListOverlay(viewModel: TodoListViewModel = koinViewModel()) {
    val overlayState by viewModel.overlayState.collectAsStateWithLifecycle()
    val nativeShareLauncher = rememberNativeShareLauncher()
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    if (overlayState.showAddTodoSheet) {
        TodoFormBottomSheet(
            editingItem = null,
            onConfirm = { text, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(text, isPrioritized))
            },
            onDismiss = { viewModel.onAction(TodoListAction.OnDismissOverlay) }
        )
    }

    if (overlayState.showAddTodoFullScreenDialog) {
        TodoFormFullScreenDialog(
            onConfirm = { text, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmAddTodo(text, isPrioritized))
            },
            onDismiss = { viewModel.onAction(TodoListAction.OnDismissOverlay) }
        )
    }

    overlayState.editingItem?.let { item ->
        TodoFormBottomSheet(
            editingItem = item,
            onConfirm = { text, isPrioritized ->
                viewModel.onAction(TodoListAction.OnConfirmEditTodo(item.id, text, isPrioritized))
            },
            onDismiss = { viewModel.onAction(TodoListAction.OnDismissOverlay) }
        )
    }

    overlayState.shareItem?.let { item ->
        TodoShareBottomSheet(
            itemText = item.text,
            onShareClick = {
                val launcher = nativeShareLauncher
                if (launcher != null) {
                    launcher(item.text)
                } else {
                    coroutineScope.launch {
                        clipboard.setClipEntry(createPlainTextClipEntry(item.text))
                        viewModel.onAction(TodoListAction.OnCopyFallbackUsed)
                    }
                }
            },
            onDismiss = { viewModel.onAction(TodoListAction.OnDismissOverlay) }
        )
    }
}

fun todoListTab(): NavigationTab = NavigationTab(
    label = "Todo",
    icon = Icons.AutoMirrored.Filled.List,
    startDestination = TodoListRoute.List,
    entries = { todoListEntries() },
    fab = { TodoListFab() },
    overlay = { TodoListOverlay() }
)
