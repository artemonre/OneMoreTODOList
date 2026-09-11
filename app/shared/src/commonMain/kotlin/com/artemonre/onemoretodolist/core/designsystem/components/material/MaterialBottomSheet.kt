package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first

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
    onExpanded: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    // The default partially-expanded state relies on a swipe-up-to-expand gesture that isn't
    // discoverable with a mouse - on desktop the sheet just stays stuck at peek height with
    // its bottom content (e.g. the confirm/cancel row) cut off. Skipping straight to fully
    // expanded avoids that on every platform.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Lets a caller (e.g. a form that autofocuses/opens the keyboard) wait for the sheet's entrance
    // animation to settle instead of racing it - see TodoFormBottomSheet.
    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.currentValue }.first { it == SheetValue.Expanded }
        onExpanded()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = if (showDragHandle) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            null
        },
        content = content
    )
}
