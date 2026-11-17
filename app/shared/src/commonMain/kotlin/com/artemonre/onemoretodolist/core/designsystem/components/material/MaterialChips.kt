package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ChipColors
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
