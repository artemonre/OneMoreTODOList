package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.presentation.resourceLabels
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.checklist_title
import onemoretodolist.app.shared.generated.resources.scan_review_back
import onemoretodolist.app.shared.generated.resources.scan_review_add_line
import onemoretodolist.app.shared.generated.resources.scan_review_add_todos
import onemoretodolist.app.shared.generated.resources.scan_review_continue
import onemoretodolist.app.shared.generated.resources.scan_review_line_placeholder
import onemoretodolist.app.shared.generated.resources.scan_review_merge_line
import onemoretodolist.app.shared.generated.resources.scan_review_mode_separate
import onemoretodolist.app.shared.generated.resources.scan_review_mode_single
import onemoretodolist.app.shared.generated.resources.scan_review_remove_line
import onemoretodolist.app.shared.generated.resources.scan_review_split_line
import onemoretodolist.app.shared.generated.resources.scan_review_title
import onemoretodolist.app.shared.generated.resources.scan_review_todo_text
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

// Separate: every row becomes its own todo. Single: the first row is the todo's text and the
// rest become its checklist.
enum class ScannedTextMode { Separate, Single }

// Ids keep each row's text field (and its focus) stable while rows are removed or added. parts are
// the lines a joined row was built from - more than one means it can still be split back apart.
// Editing the text makes it one part, since the old lines no longer match it.
private data class ScannedLine(val id: String, val text: String, val parts: List<String> = listOf(text))

// The edit step between recognizing a photo's text and saving it - OCR output is rarely clean, so
// every row can be fixed, merged into the one above, split back into its lines, removed or added
// to first. Rows start as paragraphs - the wrapped-line guess can be wrong either way, hence both
// merge and split. Discard/back (onDismiss) is a step back, not an exit - the caller reopens the
// camera for another try. Separate saves straight away; Single hands over to
// TodoFormFullScreenDialog (prefilled) so tags, due time and recurrence can still be set. Blank
// rows are dropped either way.
@OptIn(ExperimentalUuidApi::class)
@Composable
fun ScannedTextReviewDialog(
    paragraphs: List<ScannedParagraph>,
    onConfirmSeparate: (lines: List<String>) -> Unit,
    onContinueAsSingle: (text: String, checklist: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(ScannedTextMode.Separate) }
    var rows by remember {
        mutableStateOf(paragraphs.map { ScannedLine(id = Uuid.random().toString(), text = it.text, parts = it.lines) })
    }
    var justAddedId by remember { mutableStateOf<String?>(null) }
    val nonBlankLines = rows.map { it.text.trim() }.filter { it.isNotEmpty() }

    FullScreenFormDialog(onDismiss = onDismiss) { requestDismiss ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(Res.string.scan_review_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics { heading() }
            )
            Spacer(Modifier.height(16.dp))
            EnumSegmentedRow(
                options = ScannedTextMode.entries,
                selected = mode,
                onSelected = { mode = it },
                label = resourceLabels(ScannedTextMode.entries) { it.displayName() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            rows.forEachIndexed { index, row ->
                if (mode == ScannedTextMode.Single && index <= 1) {
                    if (index == 1) Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(if (index == 0) Res.string.scan_review_todo_text else Res.string.checklist_title),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.semantics { heading() }
                    )
                    Spacer(Modifier.height(4.dp))
                }
                val focusRequester = remember(row.id) { FocusRequester() }
                if (row.id == justAddedId) {
                    LaunchedEffect(row.id) {
                        focusRequester.requestFocus()
                        justAddedId = null
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = CHECKLIST_ROW_BACKGROUND_ALPHA),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChecklistItemTextField(
                        value = row.text,
                        onValueChange = { newText ->
                            rows = rows.map { if (it.id == row.id) ScannedLine(it.id, newText) else it }
                        },
                        placeholder = stringResource(Res.string.scan_review_line_placeholder),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        // A paragraph can run long - wrapped, it stays readable without scrolling sideways.
                        singleLine = false
                    )
                    if (row.parts.size > 1) {
                        IconButton(onClick = { rows = rows.splitAt(index) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CallSplit,
                                contentDescription = stringResource(Res.string.scan_review_split_line)
                            )
                        }
                    }
                    // OCR often breaks one sentence over several lines - join it back onto the
                    // line above. The first row has nothing above it, so it keeps an empty slot of
                    // the same size to stay aligned with the rest.
                    if (index > 0) {
                        IconButton(onClick = { rows = rows.mergedIntoPrevious(index) }) {
                            Icon(
                                imageVector = Icons.Filled.Merge,
                                contentDescription = stringResource(Res.string.scan_review_merge_line)
                            )
                        }
                    } else {
                        Spacer(Modifier.minimumInteractiveComponentSize())
                    }
                    IconButton(onClick = { rows = rows.filterNot { it.id == row.id } }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(Res.string.scan_review_remove_line)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            TextButton(
                onClick = {
                    val newRow = ScannedLine(id = Uuid.random().toString(), text = "")
                    justAddedId = newRow.id
                    rows = rows + newRow
                }
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.scan_review_add_line))
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = requestDismiss) {
                    Text(stringResource(Res.string.scan_review_back))
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        when (mode) {
                            ScannedTextMode.Separate -> onConfirmSeparate(nonBlankLines)
                            ScannedTextMode.Single -> onContinueAsSingle(nonBlankLines.first(), nonBlankLines.drop(1))
                        }
                    },
                    enabled = nonBlankLines.isNotEmpty()
                ) {
                    Text(
                        when (mode) {
                            ScannedTextMode.Separate -> pluralStringResource(
                                Res.plurals.scan_review_add_todos,
                                nonBlankLines.size,
                                nonBlankLines.size
                            )
                            ScannedTextMode.Single -> stringResource(Res.string.scan_review_continue)
                        }
                    )
                }
            }
        }
    }
}

// Appends the row at index to the one above it (joined like wrapped lines - see joinWrappedLines)
// and removes it - the merged row keeps the upper row's id, so its field stays put, and both rows'
// parts, so a split undoes the merge.
private fun List<ScannedLine>.mergedIntoPrevious(index: Int): List<ScannedLine> {
    val previous = this[index - 1]
    val merged = previous.copy(
        text = joinWrappedLines(listOf(previous.text, this[index].text)),
        parts = previous.parts + this[index].parts
    )
    return toMutableList().apply {
        this[index - 1] = merged
        removeAt(index)
    }
}

// Replaces the row at index with one row per part - the first keeps the row's id.
@OptIn(ExperimentalUuidApi::class)
private fun List<ScannedLine>.splitAt(index: Int): List<ScannedLine> {
    val split = this[index].parts.mapIndexed { partIndex, part ->
        ScannedLine(id = if (partIndex == 0) this[index].id else Uuid.random().toString(), text = part)
    }
    return take(index) + split + drop(index + 1)
}

private fun ScannedTextMode.displayName(): StringResource = when (this) {
    ScannedTextMode.Separate -> Res.string.scan_review_mode_separate
    ScannedTextMode.Single -> Res.string.scan_review_mode_single
}

@Preview
@Composable
private fun ScannedTextReviewDialogPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        ScannedTextReviewDialog(
            paragraphs = listOf(
                ScannedParagraph(listOf("Pick up the parcel at the post office", "before it closes on Friday")),
                ScannedParagraph(listOf("Milk")),
                ScannedParagraph(listOf("Bread"))
            ),
            onConfirmSeparate = {},
            onContinueAsSingle = { _, _ -> },
            onDismiss = {}
        )
    }
}
