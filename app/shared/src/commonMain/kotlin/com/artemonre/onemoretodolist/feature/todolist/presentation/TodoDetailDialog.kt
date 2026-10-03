package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.createPlainTextClipEntry
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import com.artemonre.onemoretodolist.feature.todolist.domain.TopTodoAttention
import com.artemonre.onemoretodolist.hasNativeCopyConfirmation
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.action_close
import onemoretodolist.app.shared.generated.resources.detail_attention_info
import org.jetbrains.compose.resources.stringResource

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
    onCopied: () -> Unit,
    onToggleChecklistItem: (itemId: String) -> Unit = {}
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    MaterialAlertDialog(
        onDismissRequest = onDismiss,
        // Tighter than stock below the text: half the bottom padding, two-thirds of the text-to-buttons gap.
        contentPadding = PaddingValues(start = AppSpacing.xl, top = AppSpacing.xl, end = AppSpacing.xl, bottom = AppSpacing.m),
        buttonsTopSpacing = AppSpacing.l,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_close))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (item.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        modifier = Modifier.padding(bottom = AppSpacing.m)
                    ) {
                        item.tags.forEach { TodoTagChip(it) }
                    }
                }
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
                // The one place checklist items get ticked off outside the edit form - each row is a
                // single toggleable target, checkbox and text together.
                if (item.checklist.isNotEmpty()) {
                    Spacer(Modifier.height(AppSpacing.m))
                    item.checklist.forEach { entry ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = entry.isDone,
                                    onValueChange = { onToggleChecklistItem(entry.id) },
                                    role = Role.Checkbox
                                )
                                .padding(vertical = AppSpacing.xs)
                        ) {
                            AppCheckToggle(checked = entry.isDone, onCheckedChange = null)
                            Spacer(Modifier.width(AppSpacing.s))
                            Text(
                                text = entry.text,
                                textDecoration = if (entry.isDone) TextDecoration.LineThrough else null,
                                color = if (entry.isDone) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
                // Explains the attention color rather than leaving it a mystery - only shown once
                // the color has actually kicked in (attention != None), using the same day count
                // and card color that drive it.
                val daysAtTop = item.daysAtTop
                if (item.attention != TopTodoAttention.None && daysAtTop != null) {
                    Text(
                        text = stringResource(Res.string.detail_attention_info, daysAtTop),
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
                creationDate = LocalDate(2026, 8, 24),
                checklist = listOf(
                    ChecklistItem(id = "a", text = "Outline modules", isDone = true),
                    ChecklistItem(id = "b", text = "Describe data flow")
                ),
                tags = listOf(TodoTag("Work", TagColor.Blue))
            ),
            onDismiss = {},
            onCopied = {}
        )
    }
}
