package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalWindowInfo
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.action_close
import onemoretodolist.app.shared.generated.resources.detail_attention_info
import onemoretodolist.app.shared.generated.resources.detail_reminder_info
import onemoretodolist.app.shared.generated.resources.mascot_ball_sheep
import onemoretodolist.app.shared.generated.resources.mascot_head_leaning_on_fence
import onemoretodolist.app.shared.generated.resources.mascot_head_leaning_on_fence_monochrome
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val MASCOT_SIZE = 160.dp
// Where the drawing actually starts (the ear tips) and where its fence line runs, as fractions of its
// square canvas. The card's top edge goes on the fence line, so the head peeks out from behind the
// card while the paws rest on it.
private const val MASCOT_EARS_FRACTION = 0.3f
private const val MASCOT_FENCE_FRACTION = 0.65f
private val MASCOT_OVERHANG = peekingMascotOverhang(MASCOT_SIZE, MASCOT_EARS_FRACTION, MASCOT_FENCE_FRACTION)

// The sheep below the card, with a small gap between the two.
private val SHEEP_SIZE = 96.dp
private val SHEEP_STRIP_HEIGHT = SHEEP_SIZE + AppSpacing.s

// The card never grows past this share of the screen's height - a long text or checklist scrolls
// inside it instead.
private const val MAX_CARD_HEIGHT_SCREEN_FRACTION = 0.6f

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
    val shareText = rememberShareText(item)
    // Read here, outside the dialog, so it's the app window's height rather than the dialog's own.
    val maxCardHeight = LocalWindowInfo.current.containerDpSize.height * MAX_CARD_HEIGHT_SCREEN_FRACTION

    MaterialAlertDialog(
        onDismissRequest = onDismiss,
        // The mascot's overhang sits above the card, so it doesn't count toward the card's cap.
        // fillMaxWidth keeps the card at the dialog's max width whatever the text length, so
        // short and long todos look the same.
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxCardHeight + MASCOT_OVERHANG + SHEEP_STRIP_HEIGHT),
        // Tighter than stock below the text: half the bottom padding, two-thirds of the text-to-buttons gap.
        contentPadding = PaddingValues(start = AppSpacing.xl, top = AppSpacing.xl, end = AppSpacing.xl, bottom = AppSpacing.m),
        buttonsTopSpacing = AppSpacing.l,
        topDecoration = {
            PeekingMascot(
                mascotDrawable(Res.drawable.mascot_head_leaning_on_fence, Res.drawable.mascot_head_leaning_on_fence_monochrome),
                MASCOT_SIZE,
                MASCOT_EARS_FRACTION
            )
        },
        topDecorationOverhang = MASCOT_OVERHANG,
        bottomDecoration = {
            Image(
                painter = painterResource(Res.drawable.mascot_ball_sheep),
                contentDescription = null,
                modifier = Modifier.size(SHEEP_SIZE)
            )
        },
        bottomDecorationHeight = SHEEP_STRIP_HEIGHT,
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
                            clipboard.setClipEntry(createPlainTextClipEntry(shareText))
                            if (!hasNativeCopyConfirmation) onCopied()
                        }
                    }
                )
                // The closest reminder, same as the card's bottom-left label but spelled out in full.
                item.nextReminder?.let { instant ->
                    val at = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                    Text(
                        text = stringResource(
                            Res.string.detail_reminder_info,
                            rememberShortDateFormat().format(at.date),
                            dueTimeFormat.format(at.time)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.s)
                    )
                }
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
