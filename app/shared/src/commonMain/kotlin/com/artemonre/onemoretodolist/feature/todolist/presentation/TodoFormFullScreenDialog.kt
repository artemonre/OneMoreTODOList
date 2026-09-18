package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence

// decorFitsSystemWindows = false (Android only - see the .android.kt actual) hands insets
// (including the keyboard's) to Compose instead of the OS - without it, WindowInsets.safeDrawing
// below reports a 0 ime inset and the form's own verticalScroll can't tell there's a keyboard to
// scroll past, so the Create/Discard row can end up hidden behind it. Other platforms don't have
// this property at all, hence expect/actual rather than a plain shared DialogProperties(...) call.
internal expect fun fullScreenDialogProperties(): DialogProperties

// The modern (non-DialogFragment) replacement for a full-screen modal: a full-size Dialog hosting
// the same TodoFormBody used by the quick-add bottom sheet. Used for both the detailed add flow
// and editing an existing todo - editingItem == null means "add" mode, same convention as
// TodoFormBottomSheet. The extra room here (vs. the quick-add bottom sheet/widget screen) is what
// fits recurrence configuration, so this is the only host that turns showRecurrence on.
@Composable
fun TodoFormFullScreenDialog(
    editingItem: TodoItemUi?,
    onConfirm: (text: String, isPrioritized: Boolean, recurrence: Recurrence?) -> Unit,
    onDismiss: () -> Unit
) {
    // A Dialog has no built-in enter/exit transition on any target here - animate the content in
    // and out ourselves (rather than relying on platform window animations, which differ per
    // target). Dismissing only sets targetState to false; the LaunchedEffect below waits for the
    // slide-down to actually finish before tearing down the Dialog window via the real onDismiss -
    // otherwise it would just vanish instantly mid-animation.
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val requestDismiss: () -> Unit = { visibleState.targetState = false }

    LaunchedEffect(visibleState) {
        snapshotFlow { visibleState.currentState to visibleState.targetState }
            .collect { (current, target) -> if (!target && current == target) onDismiss() }
    }

    Dialog(
        onDismissRequest = requestDismiss,
        properties = fullScreenDialogProperties()
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                TodoFormBody(
                    editingItem = editingItem,
                    onConfirm = onConfirm,
                    onDismiss = requestDismiss,
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .verticalScroll(rememberScrollState()),
                    fieldMaxLines = Int.MAX_VALUE,
                    showRecurrence = true,
                    requireText = true
                )
            }
        }
    }
}

@Preview
@Composable
private fun TodoFormFullScreenDialogPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoFormFullScreenDialog(
            editingItem = null,
            onConfirm = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
