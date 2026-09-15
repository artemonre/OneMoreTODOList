package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.createPlainTextClipEntry
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TopTodoAttention
import com.artemonre.onemoretodolist.hasNativeCopyConfirmation
import kotlinx.coroutines.launch

private val URL_REGEX = Regex("""(?:https?://|www\.)\S+""")
private val TRAILING_URL_PUNCTUATION = ".,;:!?)]}\"'".toSet()

// Turns any http(s)/www links in a todo's text into clickable, underlined spans - the rest of the
// text (and taps outside any link) still falls through to the Text's own clickable-to-copy.
private fun linkify(text: String, linkColor: Color): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    for (match in URL_REGEX.findAll(text)) {
        val start = match.range.first
        var end = match.range.last + 1
        // A link at the end of a sentence shouldn't swallow its own punctuation.
        while (end > start && text[end - 1] in TRAILING_URL_PUNCTUATION) end--
        append(text.substring(cursor, start))
        val url = text.substring(start, end)
        val href = if (url.startsWith("http")) url else "https://$url"
        withLink(
            LinkAnnotation.Url(
                url = href,
                styles = TextLinkStyles(
                    style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                )
            )
        ) {
            append(url)
        }
        cursor = end
    }
    append(text.substring(cursor))
}

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
            Column {
                val linkColor = MaterialTheme.colorScheme.primary
                Text(
                    text = remember(item.text, linkColor) { linkify(item.text, linkColor) },
                    modifier = Modifier.clickable {
                        coroutineScope.launch {
                            clipboard.setClipEntry(createPlainTextClipEntry(item.text))
                            if (!hasNativeCopyConfirmation) onCopied()
                        }
                    }
                )
                // Explains the attention color rather than leaving it a mystery - only shown once
                // the color has actually kicked in (attention != None), using the same day count
                // and card color that drive it.
                val daysAtTop = item.daysAtTop
                if (item.attention != TopTodoAttention.None && daysAtTop != null) {
                    Text(
                        text = "Info:\nThis todo wasn't interacted in $daysAtTop days, maybe you should change or replace it",
                        style = MaterialTheme.typography.bodySmall,
                        color = item.attention.containerColor() ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
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
                formattedDate = "24 Aug 2026"
            ),
            onDismiss = {},
            onCopied = {}
        )
    }
}
