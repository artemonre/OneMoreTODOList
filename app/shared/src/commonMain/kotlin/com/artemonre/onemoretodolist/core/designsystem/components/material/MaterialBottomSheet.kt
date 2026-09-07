package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A stock Material3 [ModalBottomSheet] - default shape, colors, and scrim all from
 * `BottomSheetDefaults`, no customizations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        // The default partially-expanded state relies on a swipe-up-to-expand gesture that isn't
        // discoverable with a mouse - on desktop the sheet just stays stuck at peek height with
        // its bottom content (e.g. the confirm/cancel row) cut off. Skipping straight to fully
        // expanded avoids that on every platform.
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = if (showDragHandle) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            null
        },
        content = content
    )
}
