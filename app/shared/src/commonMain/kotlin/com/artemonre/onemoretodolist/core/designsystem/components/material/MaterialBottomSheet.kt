package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
    // Drawn centered over the sheet's top edge, on top of it - sticks out above the sheet by
    // topDecorationOverhang, and whatever's taller than that overlaps onto the sheet.
    topDecoration: @Composable (() -> Unit)? = null,
    topDecorationOverhang: Dp = 0.dp,
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

    if (topDecoration == null) {
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
        return
    }

    val scope = rememberCoroutineScope()
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)

    // The stock sheet clips its content to its own shape, so nothing can stick out above it. Instead
    // the stock sheet itself is left invisible and an identical-looking sheet is drawn inside it,
    // topDecorationOverhang lower - the decoration fills the gap. Drag handle and insets move inside
    // with it so they stay on the visible sheet.
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RectangleShape,
        containerColor = Color.Transparent,
        contentColor = contentColorFor(BottomSheetDefaults.ContainerColor),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        // propagateMinConstraints keeps the visible sheet at full width - the decoration itself
        // opts out of it via wrapContentSize.
        Box(propagateMinConstraints = true) {
            Surface(
                modifier = Modifier.padding(top = topDecorationOverhang),
                shape = BottomSheetDefaults.ExpandedShape,
                color = BottomSheetDefaults.ContainerColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(BottomSheetDefaults.modalWindowInsets)
                ) {
                    if (showDragHandle) {
                        BottomSheetDefaults.DragHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    content()
                }
            }
            // The gap above the visible sheet is still the (invisible) stock sheet, not scrim, so taps
            // there - beside the decoration or on its part above the sheet - would otherwise do
            // nothing. Dismiss like a scrim tap instead. A plain tap detector rather than clickable,
            // so drags still reach the sheet and the scrim stays the only "close" for accessibility.
            Box(
                Modifier
                    .matchParentSize()
                    .wrapContentHeight(Alignment.Top)
                    .height(topDecorationOverhang)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) currentOnDismissRequest()
                            }
                        }
                    }
            )
            Box(Modifier.wrapContentSize(Alignment.TopCenter)) { topDecoration() }
        }
    }
}
