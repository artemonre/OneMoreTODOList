package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val FLIP_DURATION_MS = 1000
private const val FLIP_BACK_DEGREES = 180f
private const val FLIP_HALFWAY_DEGREES = 90f

/**
 * A 180-degree flip around the X axis instead of a fade/scale - positive rotationX means the top
 * edge swings toward the viewer and the bottom edge swings away while [visible] becomes false, and
 * the exact reverse (top swings away, bottom swings toward) while it becomes true again, since
 * that's just the same rotation played backward. There's no actual "back face" content, so once
 * past the halfway point (edge-on to the viewer) it's snapped invisible instead of showing a
 * mirrored [content].
 */
@Composable
fun AppFlipVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isComposed by remember { mutableStateOf(visible) }
    if (visible) isComposed = true

    val flipDegrees by animateFloatAsState(
        targetValue = if (visible) 0f else FLIP_BACK_DEGREES,
        animationSpec = tween(durationMillis = FLIP_DURATION_MS, easing = FastOutSlowInEasing),
        label = "AppFlipVisibility",
        finishedListener = { if (!visible) isComposed = false }
    )

    if (isComposed) {
        Box(
            modifier = modifier.graphicsLayer {
                rotationX = flipDegrees
                alpha = if (flipDegrees <= FLIP_HALFWAY_DEGREES) 1f else 0f
            }
        ) {
            content()
        }
    }
}
