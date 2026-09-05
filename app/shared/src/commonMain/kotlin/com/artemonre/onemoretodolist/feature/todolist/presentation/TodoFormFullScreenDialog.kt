package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// The modern (non-DialogFragment) replacement for a full-screen modal: a full-size Dialog
// hosting the same TodoFormBody used by the edit flow's bottom sheet. Add-only for now - editing
// still goes through TodoFormBottomSheet.
@Composable
fun TodoFormFullScreenDialog(
    onConfirm: (text: String, isPrioritized: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            TodoFormBody(
                editingItem = null,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Preview
@Composable
private fun TodoFormFullScreenDialogPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoFormFullScreenDialog(
            onConfirm = { _, _ -> },
            onDismiss = {}
        )
    }
}
