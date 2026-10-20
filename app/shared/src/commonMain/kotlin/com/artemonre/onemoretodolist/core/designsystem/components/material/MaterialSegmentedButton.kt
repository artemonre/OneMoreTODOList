package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A stock Material3 single-choice [SegmentedButton] - default colors, border, and icon from
 * [SegmentedButtonDefaults]. Exists so the inner [contentPadding] is set in one place. Shaped by
 * its [index] among [count] buttons in the row.
 */
@Composable
fun SingleChoiceSegmentedButtonRowScope.MaterialSegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    index: Int,
    count: Int,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = SegmentedButtonDefaults.ContentPadding
) {
    SegmentedButton(
        selected = selected,
        onClick = onClick,
        shape = SegmentedButtonDefaults.itemShape(index = index, count = count),
        modifier = modifier,
        contentPadding = contentPadding,
        label = label
    )
}
