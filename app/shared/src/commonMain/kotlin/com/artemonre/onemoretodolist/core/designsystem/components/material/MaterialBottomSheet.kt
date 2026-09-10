package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.artemonre.onemoretodolist.PredictiveBackHandler
import kotlin.math.roundToInt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// The sheet's own settled positions: Hidden sits fully below the screen (at its own measured
// height), Expanded sits flush with the bottom - no PartiallyExpanded state, this project always
// skips straight to fully expanded.
private enum class DragAnchor { Hidden, Expanded }

// Predictive back shrinks the sheet slightly as the gesture progresses, matching the platform's
// usual "peeling away" preview.
private const val PREDICTIVE_BACK_MIN_SCALE = 0.95f

/**
 * A hand-rolled modal bottom sheet - scrim + slide-up [Surface], rendered inline in the same
 * window as the rest of the screen rather than in a separate Android Dialog like Material3's own
 * ModalBottomSheet. Keeping everything in one window removes the window-focus latency and
 * decoupled animation timelines that made the keyboard's appearance visibly race the sheet's own
 * entrance animation - see TodoFormBottomSheet, which is why this exists.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val dragState = remember { AnchoredDraggableState(initialValue = DragAnchor.Hidden) }
    var backScale by remember { mutableFloatStateOf(1f) }

    // Slide up once the sheet's real height is known (updateAnchors below).
    LaunchedEffect(dragState) {
        snapshotFlow { dragState.offset }.first { !it.isNaN() }
        dragState.animateTo(DragAnchor.Expanded)
    }

    // Fires once the state settles back at Hidden, from any path: swipe-down, scrim tap, or a
    // completed predictive-back gesture (all of which just drive the same dragState).
    LaunchedEffect(dragState) {
        snapshotFlow { dragState.settledValue }.drop(1).first { it == DragAnchor.Hidden }
        onDismissRequest()
    }

    // Subsumes a plain back-button press too (the real PredictiveBackHandler delivers those as an
    // immediately-completing progress Flow), so no separate BackHandler is needed alongside this.
    PredictiveBackHandler(enabled = true) { progress ->
        try {
            progress.collect { fraction ->
                backScale = 1f - (1f - PREDICTIVE_BACK_MIN_SCALE) * fraction
            }
            backScale = 1f
            dragState.animateTo(DragAnchor.Hidden)
        } catch (e: CancellationException) {
            backScale = 1f
            dragState.animateTo(DragAnchor.Expanded)
            throw e
        }
    }

    // imePadding() here (on the shared container, not the Surface itself) shrinks the space both
    // the scrim and the sheet lay out within, so Alignment.BottomCenter below naturally re-settles
    // the sheet's resting position against the keyboard as it opens - matching Material3's own
    // ModalBottomSheet structure (Box.fillMaxSize().imePadding(), sheet aligned within it), which
    // needs no manual keyboard-height math at all.
    Box(modifier = modifier.fillMaxSize().imePadding()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = dragState.progress(DragAnchor.Hidden, DragAnchor.Expanded) }
                .background(BottomSheetDefaults.ScrimColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { coroutineScope.launch { dragState.animateTo(DragAnchor.Hidden) } }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { size ->
                    dragState.updateAnchors(
                        DraggableAnchors {
                            DragAnchor.Hidden at size.height.toFloat()
                            DragAnchor.Expanded at 0f
                        }
                    )
                }
                .offset { IntOffset(0, dragState.requireOffset().roundToInt()) }
                .anchoredDraggable(dragState, Orientation.Vertical)
                .graphicsLayer {
                    scaleX = backScale
                    scaleY = backScale
                },
            shape = BottomSheetDefaults.ExpandedShape,
            color = BottomSheetDefaults.ContainerColor
        ) {
            Column {
                if (showDragHandle) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        BottomSheetDefaults.DragHandle()
                    }
                }
                content()
            }
        }
    }
}
