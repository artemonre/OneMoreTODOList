package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.components.keepsKeyboardOnTap
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAssistChip
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialInputChip
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialOutlinedTextField
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialSuggestionChip
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.checklist_add_item
import onemoretodolist.app.shared.generated.resources.checklist_item_placeholder
import onemoretodolist.app.shared.generated.resources.checklist_remove_item
import onemoretodolist.app.shared.generated.resources.checklist_title
import onemoretodolist.app.shared.generated.resources.form_add
import onemoretodolist.app.shared.generated.resources.form_cancel
import onemoretodolist.app.shared.generated.resources.tag_add
import onemoretodolist.app.shared.generated.resources.tag_color
import onemoretodolist.app.shared.generated.resources.tag_color_blue
import onemoretodolist.app.shared.generated.resources.tag_color_green
import onemoretodolist.app.shared.generated.resources.tag_color_orange
import onemoretodolist.app.shared.generated.resources.tag_color_pink
import onemoretodolist.app.shared.generated.resources.tag_color_purple
import onemoretodolist.app.shared.generated.resources.tag_color_red
import onemoretodolist.app.shared.generated.resources.tag_color_teal
import onemoretodolist.app.shared.generated.resources.tag_color_yellow
import onemoretodolist.app.shared.generated.resources.tag_name
import onemoretodolist.app.shared.generated.resources.tag_new_title
import onemoretodolist.app.shared.generated.resources.tag_remove
import onemoretodolist.app.shared.generated.resources.tags_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val TAG_COLOR_SWATCH_SIZE = 32.dp
private val TAG_COLOR_SELECTED_BORDER = 2.dp
private val CHECKLIST_FIELD_PADDING = AppSpacing.xs
// How much of surfaceContainer (the palette's main blue) shows through behind each checklist row -
// just enough to group checkbox, text and remove button without it reading as a filled box.
private const val CHECKLIST_ROW_BACKGROUND_ALPHA = 0.12f

// The tags a todo carries, as removable colored chips, plus an "Add tag" chip that opens
// AddTagDialog. Only the full-screen form shows this - see TodoFormBody's showTags.
@Composable
internal fun TodoFormTagsSection(
    tags: List<TodoTag>,
    knownTags: List<TodoTag>,
    onTagsChange: (List<TodoTag>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(Res.string.tags_title), style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(AppSpacing.xs))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)) {
            tags.forEach { tag ->
                val swatch = tag.color.swatch()
                val removeDescription = stringResource(Res.string.tag_remove, tag.name)
                MaterialInputChip(
                    selected = false,
                    onClick = { onTagsChange(tags - tag) },
                    label = { Text(tag.name) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = removeDescription,
                            modifier = Modifier.size(InputChipDefaults.IconSize)
                        )
                    },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = swatch.container,
                        labelColor = swatch.content,
                        trailingIconColor = swatch.content
                    ),
                    border = null
                )
            }
            MaterialAssistChip(
                onClick = { showAddDialog = true },
                label = { Text(stringResource(Res.string.tag_add)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }
            )
        }
    }
    if (showAddDialog) {
        AddTagDialog(
            existingTags = tags,
            knownTags = knownTags,
            onAdd = { tag ->
                onTagsChange(tags + tag)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

// Typing a name that's already in use elsewhere (case-insensitively) picks up that tag's color, so
// one name always stays one color - the color row is locked to it while the name matches.
@Composable
private fun AddTagDialog(
    existingTags: List<TodoTag>,
    knownTags: List<TodoTag>,
    onAdd: (TodoTag) -> Unit,
    onDismiss: () -> Unit
) {
    val nameState = rememberTextFieldState()
    var pickedColor by remember { mutableStateOf(TagColor.entries.random()) }
    val trimmedName = nameState.text.trim().toString()
    val knownMatch = knownTags.firstOrNull { it.name.equals(trimmedName, ignoreCase = true) }
    val color = knownMatch?.color ?: pickedColor
    val alreadyAdded = existingTags.any { it.name.equals(trimmedName, ignoreCase = true) }
    val canAdd = trimmedName.isNotEmpty() && !alreadyAdded
    val suggestions = knownTags.filter { known ->
        existingTags.none { it.name.equals(known.name, ignoreCase = true) } &&
            known.name.contains(trimmedName, ignoreCase = true)
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    MaterialAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.tag_new_title)) },
        text = {
            Column {
                MaterialOutlinedTextField(
                    state = nameState,
                    label = { Text(stringResource(Res.string.tag_name)) },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    compact = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                if (suggestions.isNotEmpty()) {
                    Spacer(Modifier.height(AppSpacing.s))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s)) {
                        suggestions.forEach { suggestion ->
                            val swatch = suggestion.color.swatch()
                            MaterialSuggestionChip(
                                onClick = { onAdd(suggestion) },
                                label = { Text(suggestion.name) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = swatch.container,
                                    labelColor = swatch.content
                                ),
                                border = null
                            )
                        }
                    }
                }
                Spacer(Modifier.height(AppSpacing.m))
                Text(text = stringResource(Res.string.tag_color), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(AppSpacing.s))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.s)
                ) {
                    TagColor.entries.forEach { option ->
                        TagColorSwatch(
                            color = option,
                            selected = option == color,
                            enabled = knownMatch == null,
                            onClick = { pickedColor = option }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(TodoTag(knownMatch?.name ?: trimmedName, color)) },
                enabled = canAdd
            ) {
                Text(stringResource(Res.string.form_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.form_cancel))
            }
        }
    )
}

@Composable
private fun TagColorSwatch(
    color: TagColor,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val description = stringResource(color.nameResource())
    Box(
        modifier = Modifier
            .size(TAG_COLOR_SWATCH_SIZE)
            .clip(CircleShape)
            .background(color.swatch().container)
            .then(
                if (selected) {
                    Modifier.border(TAG_COLOR_SELECTED_BORDER, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
            }
    )
}

private fun TagColor.nameResource(): StringResource = when (this) {
    TagColor.Red -> Res.string.tag_color_red
    TagColor.Orange -> Res.string.tag_color_orange
    TagColor.Yellow -> Res.string.tag_color_yellow
    TagColor.Green -> Res.string.tag_color_green
    TagColor.Teal -> Res.string.tag_color_teal
    TagColor.Blue -> Res.string.tag_color_blue
    TagColor.Purple -> Res.string.tag_color_purple
    TagColor.Pink -> Res.string.tag_color_pink
}

// One flat level of items - tick, edit text, remove - plus "Add item", which appends a blank item
// and focuses it. Blank items are dropped on save (see TodoFormBody). Only the full-screen form
// shows this - see TodoFormBody's showChecklist.
@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun TodoFormChecklistSection(
    items: List<ChecklistItem>,
    onItemsChange: (List<ChecklistItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    var justAddedId by remember { mutableStateOf<String?>(null) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(Res.string.checklist_title), style = MaterialTheme.typography.titleSmall)
        items.forEach { item ->
            val focusRequester = remember(item.id) { FocusRequester() }
            if (item.id == justAddedId) {
                LaunchedEffect(item.id) {
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
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckToggle(
                    checked = item.isDone,
                    onCheckedChange = { checked ->
                        onItemsChange(items.map { if (it.id == item.id) it.copy(isDone = checked) else it })
                    }
                )
                Spacer(Modifier.width(AppSpacing.s))
                ChecklistItemTextField(
                    value = item.text,
                    onValueChange = { newText ->
                        onItemsChange(items.map { if (it.id == item.id) it.copy(text = newText) else it })
                    },
                    placeholder = stringResource(Res.string.checklist_item_placeholder),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
                IconButton(onClick = { onItemsChange(items.filterNot { it.id == item.id }) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.checklist_remove_item)
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.xs))
        }
        TextButton(
            onClick = {
                val newItem = ChecklistItem(id = Uuid.random().toString(), text = "")
                justAddedId = newItem.id
                onItemsChange(items + newItem)
            }
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.checklist_add_item))
        }
    }
}

// A bare field for checklist lines: no container, border or underline, just 4dp of inner padding -
// the row around it (checkbox, text, remove button) carries the background instead.
@Composable
private fun ChecklistItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = modifier.keepsKeyboardOnTap(),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(CHECKLIST_FIELD_PADDING)) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
                innerTextField()
            }
        }
    )
}
