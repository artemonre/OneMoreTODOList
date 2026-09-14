package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
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

// editingItem == null means "add" mode; non-null pre-fills the form and confirms as an edit.
@Composable
fun TodoFormBottomSheet(
    editingItem: TodoItemUi?,
    onConfirm: (text: String, isPrioritized: Boolean, recurrence: Recurrence?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSheetExpanded by remember { mutableStateOf(false) }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        onExpanded = { isSheetExpanded = true }
    ) {
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
    awaitAutoFocusReady: suspend () -> Unit = {}
) {
    var text by remember { mutableStateOf(editingItem?.text.orEmpty()) }
    var isPrioritized by remember { mutableStateOf(editingItem?.isPrioritized ?: false) }
    var repeatEnabled by remember { mutableStateOf(editingItem?.recurrence != null) }
    var recurrenceType by remember { mutableStateOf(editingItem?.recurrence?.type ?: RecurrenceType.Every) }
    var recurrenceIntervalText by remember { mutableStateOf(editingItem?.recurrence?.interval?.toString() ?: "1") }
    var recurrenceUnit by remember { mutableStateOf(editingItem?.recurrence?.unit ?: RecurrenceUnit.Week) }
    val recurrence = if (repeatEnabled) {
        recurrenceIntervalText.toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { interval -> Recurrence(type = recurrenceType, interval = interval, unit = recurrenceUnit) }
    } else {
        null
    }
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
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(16.dp))
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("A todo text") },
            singleLine = false,
            minLines = fieldMinLines,
            maxLines = fieldMaxLines,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onConfirm(text.trim(), isPrioritized, recurrence) }
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
        Spacer(Modifier.height(12.dp))
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
            Text("Put on top")
        }
        if (showRecurrence) {
            Spacer(Modifier.height(12.dp))
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
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RecurrenceSegmentedRow(
                        options = RecurrenceType.entries,
                        selected = recurrenceType,
                        onSelected = { recurrenceType = it },
                        label = { it.displayName() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    CompactOutlinedField(
                        value = recurrenceIntervalText,
                        onValueChange = { value -> recurrenceIntervalText = value.filter { it.isDigit() } },
                        placeholder = { Text("1") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(72.dp)
                    )
                    RecurrenceSegmentedRow(
                        options = RecurrenceUnit.entries,
                        selected = recurrenceUnit,
                        onSelected = { recurrenceUnit = it },
                        label = { it.displayName() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Spacer(Modifier.height(actionsTopSpacing))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onConfirm(text.trim(), isPrioritized, recurrence) }
            ) {
                Text(if (editingItem != null) "Save" else "OK")
            }
        }
    }
}

// A 40dp-tall OutlinedTextField-styled field, matching the height of the segmented buttons it sits
// beside in the recurrence section - a plain OutlinedTextField can't go below its built-in 56dp
// minimum height, so this builds the same look directly off BasicTextField + the decoration box
// Material3 exposes for exactly this case.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        keyboardOptions = keyboardOptions,
        interactionSource = interactionSource,
        modifier = modifier.height(40.dp),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                isError = false,
                label = null,
                placeholder = placeholder,
                leadingIcon = null,
                trailingIcon = null,
                prefix = null,
                suffix = null,
                supportingText = null,
                colors = OutlinedTextFieldDefaults.colors(),
                contentPadding = OutlinedTextFieldDefaults.contentPadding(
                    start = 12.dp,
                    top = 0.dp,
                    end = 12.dp,
                    bottom = 0.dp
                ),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource
                    )
                }
            )
        }
    )
}

// One row of segmented buttons for a whole enum's worth of options - used for both RecurrenceType
// and RecurrenceUnit, stacked in a column rather than crammed into one row together.
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

private fun RecurrenceType.displayName(): String = when (this) {
    RecurrenceType.Every -> "Every"
    RecurrenceType.AfterCompletion -> "After completion"
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
            onDismiss = {}
        )
    }
}
