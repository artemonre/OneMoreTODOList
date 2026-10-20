package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Stock Material3 chips - every default comes from the matching `*ChipDefaults`. They exist so the
// inner contentPadding is set in one place instead of at each call site.

@Composable
fun MaterialFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = FilterChipDefaults.ContentPadding
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        contentPadding = contentPadding
    )
}

@Composable
fun MaterialInputChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: SelectableChipColors = InputChipDefaults.inputChipColors(),
    border: BorderStroke? = InputChipDefaults.inputChipBorder(enabled = true, selected = selected),
    contentPadding: PaddingValues = InputChipDefaults.contentPadding(
        hasAvatar = false,
        hasLeadingIcon = false,
        hasTrailingIcon = trailingIcon != null
    )
) {
    InputChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        trailingIcon = trailingIcon,
        colors = colors,
        border = border,
        contentPadding = contentPadding
    )
}

@Composable
fun MaterialAssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = AssistChipDefaults.ContentPadding
) {
    AssistChip(
        onClick = onClick,
        label = label,
        modifier = modifier,
        leadingIcon = leadingIcon,
        contentPadding = contentPadding
    )
}

@Composable
fun MaterialSuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    colors: ChipColors = SuggestionChipDefaults.suggestionChipColors(),
    border: BorderStroke? = SuggestionChipDefaults.suggestionChipBorder(enabled = true),
    contentPadding: PaddingValues = SuggestionChipDefaults.ContentPadding
) {
    SuggestionChip(
        onClick = onClick,
        label = label,
        modifier = modifier,
        colors = colors,
        border = border,
        contentPadding = contentPadding
    )
}
