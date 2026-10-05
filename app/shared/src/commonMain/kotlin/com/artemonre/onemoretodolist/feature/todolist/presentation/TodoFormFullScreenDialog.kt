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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.ConfigureSystemBarIcons
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import com.artemonre.onemoretodolist.feature.todolist.domain.isDueTimeUiSupported

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
// fits due time and recurrence configuration, so this is the only host that turns showDueTime and
// showRecurrence on.
@Composable
fun TodoFormFullScreenDialog(
    editingItem: TodoItemUi?,
    onConfirm: (TodoDraft) -> Unit,
    onDismiss: () -> Unit,
    // Only used when editingItem == null - seeds the field from a draft carried over from
    // TodoFormBottomSheet's "More settings" button.
    initialText: String = "",
    knownTags: List<TodoTag> = emptyList()
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
        // A Dialog opens its own platform window, separate from the host Activity's, so it doesn't
        // inherit the icon appearance AppTheme configured there - set it again for this window.
        ConfigureSystemBarIcons(darkIcons = MaterialTheme.colorScheme.surface.luminance() > 0.5f)
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
                    showDueTime = isDueTimeUiSupported,
                    showRecurrence = true,
                    showTags = true,
                    showChecklist = true,
                    knownTags = knownTags,
                    requireText = true,
                    initialText = initialText
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
            onConfirm = {},
            onDismiss = {}
        )
    }
}
