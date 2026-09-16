package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.SpeechRecognitionState
import com.artemonre.onemoretodolist.core.designsystem.components.AppBottomSheet
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.rememberSpeechToText
import kotlinx.coroutines.flow.first
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.recurrence_after_completion
import onemoretodolist.app.shared.generated.resources.recurrence_every
import onemoretodolist.app.shared.generated.resources.recurrence_unit_day
import onemoretodolist.app.shared.generated.resources.recurrence_unit_month
import onemoretodolist.app.shared.generated.resources.recurrence_unit_week
import onemoretodolist.app.shared.generated.resources.recurrence_unit_year
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

// editingItem == null means "add" mode; non-null pre-fills the form and confirms as an edit.
@Composable
fun TodoFormBottomSheet(
    editingItem: TodoItemUi?,
    onConfirm: (text: String, isPrioritized: Boolean, recurrence: Recurrence?) -> Unit,
    onDismiss: () -> Unit,
    onMoreSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSheetExpanded by remember { mutableStateOf(false) }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        onExpanded = { isSheetExpanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick add",
                style = MaterialTheme.typography.titleLarge
            )
            TextButton(onClick = onMoreSettingsClick) {
                Text("More settings")
            }
        }
        TodoFormBody(
            editingItem = editingItem,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            // A short window (JVM/desktop, or a small/rotated phone) can leave less height than
            // the form needs - without this the Cancel/OK row can end up positioned below the
            // visible sheet instead of being reachable by scrolling.
            modifier = Modifier.verticalScroll(rememberScrollState()),
            // The sheet's own drag handle already separates it from the content above, so the
            // title is redundant here - unlike TodoFormFullScreenDialog, which has nothing else
            // marking it as an add/edit form.
            showTitle = false,
            fieldMaxLines = Int.MAX_VALUE,
            actionsTopSpacing = 8.dp,
            awaitAutoFocusReady = { snapshotFlow { isSheetExpanded }.first { it } }
        )
    }
}

// Shared by TodoFormBottomSheet (quick-add flow), TodoFormFullScreenDialog (detailed add and edit
// flows), and gateway/todoList's QuickAddTodoActivity (widget quick-add) - same fields and
// behavior, just hosted in a different container. Public since the widget's quick-add screen lives
// in a separate Gradle module.
// fieldMinLines starts at 1 line everywhere and grows with the text - TodoFormBottomSheet and
// TodoFormFullScreenDialog both raise fieldMaxLines to unlimited so it can keep growing; the
// default fieldMaxLines here (2) only applies to QuickAddTodoActivity, which doesn't override it.
// showTitle/actionsTopSpacing default to what the full-screen dialog and quick-add screen use -
// TodoFormBottomSheet tightens both since its drag handle already separates it from whatever is
// above. showRecurrence is off by default too - recurrence is a detailed-creation concern, only
// TodoFormFullScreenDialog turns it on.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoFormBody(
    editingItem: TodoItemUi?,
    onConfirm: (text: String, isPrioritized: Boolean, recurrence: Recurrence?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    fieldMinLines: Int = 1,
    fieldMaxLines: Int = 2,
    actionsTopSpacing: Dp = 20.dp,
    showRecurrence: Boolean = false,
    // Off by default: the quick-add bottom sheet and widget already rely on Android's own IME
    // "Done" handling and keep their existing no-op-on-blank behavior. TodoFormFullScreenDialog
    // turns this on since it's the one flow where a stray blank-text save was reachable.
    requireText: Boolean = false,
    awaitAutoFocusReady: suspend () -> Unit = {}
) {
    var text by remember { mutableStateOf(editingItem?.text.orEmpty()) }
    var isPrioritized by remember { mutableStateOf(editingItem?.isPrioritized ?: false) }
    var repeatEnabled by remember { mutableStateOf(editingItem?.recurrence != null) }
    var recurrenceType by remember { mutableStateOf(editingItem?.recurrence?.type ?: RecurrenceType.Every) }
    // A counter (not free text) so this is always a valid positive interval - no parsing/validation needed.
    var recurrenceInterval by remember { mutableStateOf(editingItem?.recurrence?.interval ?: 1) }
    var recurrenceUnit by remember { mutableStateOf(editingItem?.recurrence?.unit ?: RecurrenceUnit.Day) }
    val recurrence = if (repeatEnabled) {
        Recurrence(type = recurrenceType, interval = recurrenceInterval, unit = recurrenceUnit)
    } else {
        null
    }
    val canSubmit = text.isNotBlank() || !requireText
    val textFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val speechController = rememberSpeechToText(onResult = { text = it })

    LaunchedEffect(Unit) {
        // Wait for the host (bottom sheet) to finish its entrance animation before grabbing focus -
        // requesting it eagerly races the keyboard's show animation against the sheet's slide-up and
        // makes both visibly stutter.
        awaitAutoFocusReady()
        textFocusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        if (showTitle) {
            Text(
                text = if (editingItem != null) "Edit todo" else "Add todo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("A todo text") },
            placeholder = { Text("e.g., Book airline tickets") },
            singleLine = false,
            minLines = fieldMinLines,
            maxLines = fieldMaxLines,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (canSubmit) onConfirm(text.trim(), isPrioritized, recurrence) }
            ),
            trailingIcon = if (text.isNotEmpty()) {
                {
                    IconButton(onClick = { text = "" }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear text")
                    }
                }
            } else {
                speechController?.let { controller ->
                    {
                        val isListening = controller.state.value is SpeechRecognitionState.Listening
                        IconButton(
                            onClick = { if (isListening) controller.stop() else controller.start() }
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = if (isListening) "Stop listening" else "Speak todo text"
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(textFocusRequester)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = isPrioritized,
                    onValueChange = { isPrioritized = it },
                    role = Role.Checkbox
                )
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckToggle(
                checked = isPrioritized,
                onCheckedChange = null
            )
            Spacer(Modifier.width(12.dp))
            Text("Put to top")
        }
        if (showRecurrence) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = repeatEnabled,
                        onValueChange = { repeatEnabled = it },
                        role = Role.Checkbox
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckToggle(
                    checked = repeatEnabled,
                    onCheckedChange = null
                )
                Spacer(Modifier.width(12.dp))
                Text("Repeat")
            }
        }
        if (showRecurrence && repeatEnabled) {
            Spacer(Modifier.height(8.dp))
            // A real bordered section for just the recurrence controls - not the Repeat checkbox
            // above, which toggles the section rather than belonging inside it.
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Every")
                        Spacer(Modifier.width(8.dp))
                        IntervalCounter(
                            value = recurrenceInterval,
                            onValueChange = { recurrenceInterval = it }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    RecurrenceSegmentedRow(
                        options = RecurrenceUnit.entries,
                        selected = recurrenceUnit,
                        onSelected = { recurrenceUnit = it },
                        label = { it.displayName() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Padding lives on the row itself (rather than the spacedBy gap the rest of this
                    // Column used to rely on) so the toggle's clickable area grows with it - the
                    // visual spacing above/below stays the same 8dp as before.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = recurrenceType == RecurrenceType.AfterCompletion,
                                onValueChange = { afterCompletion ->
                                    recurrenceType = if (afterCompletion) {
                                        RecurrenceType.AfterCompletion
                                    } else {
                                        RecurrenceType.Every
                                    }
                                },
                                role = Role.Checkbox
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppCheckToggle(
                            checked = recurrenceType == RecurrenceType.AfterCompletion,
                            onCheckedChange = null
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Only after completion")
                    }
                    recurrence?.let { current ->
                        Text(
                            text = current.describe(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(actionsTopSpacing))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Discard")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onConfirm(text.trim(), isPrioritized, recurrence) },
                enabled = canSubmit
            ) {
                Text(if (editingItem != null) "Save" else "Create")
            }
        }
    }
}

// A minimum of 1 - going lower would either mean "never" or need a negative-interval concept
// neither Recurrence nor its UI support.
private const val MIN_RECURRENCE_INTERVAL = 1

// A -/value/+ stepper instead of free-text entry, so the interval can never be blank, zero, or
// otherwise invalid - onValueChange only ever fires with a valid positive value. Material3 has no
// spec'd counter/stepper component, so this is a custom composition of standard M3 IconButtons -
// bordered in the theme's medium (card-like) shape, rather than a hardcoded radius, so it stays
// matched if the theme's shape scale changes.
@Composable
private fun IntervalCounter(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = MaterialTheme.shapes.medium
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onValueChange(value - 1) },
            enabled = value > MIN_RECURRENCE_INTERVAL,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = "Decrease")
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(32.dp)
        )
        IconButton(
            onClick = { onValueChange(value + 1) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Increase")
        }
    }
}

// The count+unit phrase ("2 weeks") is pluralized on its own via a real plurals resource - not
// just an English-only "add an s" rule - then dropped into one of the two sentence shapes below.
// Real (locale-aware, CLDR one/few/many/other) plural resources work identically across every
// Compose Multiplatform target, not just Android.
@Composable
private fun Recurrence.describe(): String {
    val unitPhrase = pluralStringResource(unit.pluralResource(), interval, interval)
    return when (type) {
        RecurrenceType.Every -> stringResource(Res.string.recurrence_every, unitPhrase)
        RecurrenceType.AfterCompletion -> stringResource(Res.string.recurrence_after_completion, unitPhrase)
    }
}

private fun RecurrenceUnit.pluralResource(): PluralStringResource = when (this) {
    RecurrenceUnit.Day -> Res.plurals.recurrence_unit_day
    RecurrenceUnit.Week -> Res.plurals.recurrence_unit_week
    RecurrenceUnit.Month -> Res.plurals.recurrence_unit_month
    RecurrenceUnit.Year -> Res.plurals.recurrence_unit_year
}

// One row of segmented buttons for a whole enum's worth of options - currently just RecurrenceUnit,
// kept generic in case another enum picker needs the same look later.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> RecurrenceSegmentedRow(
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(label(option)) }
            )
        }
    }
}

private fun RecurrenceUnit.displayName(): String = when (this) {
    RecurrenceUnit.Day -> "Day"
    RecurrenceUnit.Week -> "Week"
    RecurrenceUnit.Month -> "Month"
    RecurrenceUnit.Year -> "Year"
}

@Preview
@Composable
private fun TodoFormBottomSheetPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoFormBottomSheet(
            editingItem = null,
            onConfirm = { _, _, _ -> },
            onDismiss = {},
            onMoreSettingsClick = {}
        )
    }
}
