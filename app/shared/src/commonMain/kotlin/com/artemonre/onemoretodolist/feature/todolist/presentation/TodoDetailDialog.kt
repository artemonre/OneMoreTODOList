package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.tooling.preview.Preview
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.createPlainTextClipEntry
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.hasNativeCopyConfirmation
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TodoDetailDialog(
    item: TodoItemUi,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Text(
                text = item.text,
                modifier = Modifier.clickable {
                    coroutineScope.launch {
                        clipboard.setClipEntry(createPlainTextClipEntry(item.text))
                        if (!hasNativeCopyConfirmation) onCopied()
                    }
                }
            )
        }
    )
}

@Preview
@Composable
private fun TodoDetailDialogPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoDetailDialog(
            item = TodoItemUi(
                id = "1",
                text = "Write project architecture document covering module boundaries, data flow, and testing strategy for the new feature",
                status = TodoStatus.Active,
                sortOrder = 0,
                formattedDate = "Aug 24, 2026"
            ),
            onDismiss = {},
            onCopied = {}
        )
    }
}
