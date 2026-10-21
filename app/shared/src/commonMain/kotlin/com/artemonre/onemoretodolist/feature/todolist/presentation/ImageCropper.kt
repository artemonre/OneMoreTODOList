package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

private val HANDLE_TOUCH_RADIUS = 32.dp
private val HANDLE_RADIUS = 8.dp
private val BORDER_WIDTH = 2.dp
private val MIN_CROP_SIZE = 48.dp
private const val SCRIM_ALPHA = 0.5f

// Which part of the frame a drag grabbed - a corner or edge resizes, inside moves the whole frame.
private enum class CropHandle { TopLeft, TopRight, BottomLeft, BottomRight, Left, Top, Right, Bottom, Move }

/**
 * Shows [image] fitted into the available space with a draggable crop frame over it. [cropRect] is
 * in the image's own pixels, so the caller can cut the original out directly - the on-screen
 * scaling stays internal.
 */
@Composable
fun ImageCropper(
    image: ImageBitmap,
    cropRect: Rect,
    onCropRectChange: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCropRect by rememberUpdatedState(cropRect)
    val currentOnCropRectChange by rememberUpdatedState(onCropRectChange)
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA)
    val frameColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier.pointerInput(image) {
            // Hit-tested where the finger lands, not where a drag gesture would start - that's
            // only reported after the touch slop, already several dp away from the corner.
            awaitEachGesture {
                val down = awaitFirstDown()
                val fit = ImageFit(image, size.width.toFloat(), size.height.toFloat())
                val grabbed = hitTest(fit.toScreen(currentCropRect), down.position, HANDLE_TOUCH_RADIUS.toPx())
                    ?: return@awaitEachGesture
                down.consume()
                drag(down.id) { change ->
                    val delta = change.positionChange()
                    change.consume()
                    currentOnCropRectChange(
                        currentCropRect.dragged(
                            handle = grabbed,
                            delta = delta / fit.scale,
                            imageSize = Size(image.width.toFloat(), image.height.toFloat()),
                            // A tiny image can be smaller than the minimum frame itself.
                            minSize = minOf(MIN_CROP_SIZE.toPx() / fit.scale, image.width.toFloat(), image.height.toFloat())
                        )
                    )
                }
            }
        }
    ) {
        val fit = ImageFit(image, size.width, size.height)
        drawImage(
            image = image,
            dstOffset = IntOffset(fit.offset.x.roundToInt(), fit.offset.y.roundToInt()),
            dstSize = IntSize((image.width * fit.scale).roundToInt(), (image.height * fit.scale).roundToInt()),
            filterQuality = FilterQuality.Medium
        )
        val frame = fit.toScreen(cropRect)
        clipRect(frame.left, frame.top, frame.right, frame.bottom, ClipOp.Difference) {
            drawRect(scrimColor, topLeft = fit.offset, size = Size(image.width * fit.scale, image.height * fit.scale))
        }
        drawRect(frameColor, topLeft = frame.topLeft, size = frame.size, style = Stroke(BORDER_WIDTH.toPx()))
        listOf(frame.topLeft, frame.topRight, frame.bottomLeft, frame.bottomRight).forEach { corner ->
            drawCircle(frameColor, radius = HANDLE_RADIUS.toPx(), center = corner)
        }
    }
}

// How the image sits on screen: scaled to fit (never cropped) and centered.
private class ImageFit(image: ImageBitmap, width: Float, height: Float) {
    val scale = min(width / image.width, height / image.height)
    val offset = Offset((width - image.width * scale) / 2, (height - image.height * scale) / 2)

    fun toScreen(rect: Rect): Rect = Rect(
        left = offset.x + rect.left * scale,
        top = offset.y + rect.top * scale,
        right = offset.x + rect.right * scale,
        bottom = offset.y + rect.bottom * scale
    )
}

// Corners win over edges, edges over the inside; a touch outside the frame grabs nothing.
private fun hitTest(frame: Rect, position: Offset, radius: Float): CropHandle? {
    fun near(point: Offset) = hypot(position.x - point.x, position.y - point.y) <= radius
    val withinX = position.x in frame.left - radius..frame.right + radius
    val withinY = position.y in frame.top - radius..frame.bottom + radius
    return when {
        near(frame.topLeft) -> CropHandle.TopLeft
        near(frame.topRight) -> CropHandle.TopRight
        near(frame.bottomLeft) -> CropHandle.BottomLeft
        near(frame.bottomRight) -> CropHandle.BottomRight
        withinY && abs(position.x - frame.left) <= radius -> CropHandle.Left
        withinY && abs(position.x - frame.right) <= radius -> CropHandle.Right
        withinX && abs(position.y - frame.top) <= radius -> CropHandle.Top
        withinX && abs(position.y - frame.bottom) <= radius -> CropHandle.Bottom
        frame.contains(position) -> CropHandle.Move
        else -> null
    }
}

// Applies one drag step in image pixels, keeping the frame inside the image and no smaller than
// minSize on either side.
private fun Rect.dragged(handle: CropHandle, delta: Offset, imageSize: Size, minSize: Float): Rect {
    if (handle == CropHandle.Move) {
        val dx = delta.x.coerceIn(-left, imageSize.width - right)
        val dy = delta.y.coerceIn(-top, imageSize.height - bottom)
        return translate(dx, dy)
    }
    val movesLeft = handle in setOf(CropHandle.TopLeft, CropHandle.BottomLeft, CropHandle.Left)
    val movesRight = handle in setOf(CropHandle.TopRight, CropHandle.BottomRight, CropHandle.Right)
    val movesTop = handle in setOf(CropHandle.TopLeft, CropHandle.TopRight, CropHandle.Top)
    val movesBottom = handle in setOf(CropHandle.BottomLeft, CropHandle.BottomRight, CropHandle.Bottom)
    return Rect(
        left = if (movesLeft) (left + delta.x).coerceIn(0f, right - minSize) else left,
        top = if (movesTop) (top + delta.y).coerceIn(0f, bottom - minSize) else top,
        right = if (movesRight) (right + delta.x).coerceIn(left + minSize, imageSize.width) else right,
        bottom = if (movesBottom) (bottom + delta.y).coerceIn(top + minSize, imageSize.height) else bottom
    )
}
