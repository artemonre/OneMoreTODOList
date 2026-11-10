package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val COUNTER_BORDER_WIDTH = 1.dp
private val COUNTER_BUTTON_SIZE = 40.dp
private val COUNTER_VALUE_WIDTH = 32.dp

/**
 * A number with − and + buttons around it, in a thin [MaterialTheme.colorScheme.outline] border -
 * Material3 has no stepper of its own, so this one is built from its icon buttons, in the medium
 * shape.
 */
@Composable
fun MaterialCounter(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseDescription: String,
    increaseDescription: String,
    modifier: Modifier = Modifier,
    decreaseEnabled: Boolean = true,
    increaseEnabled: Boolean = true
) {
    CounterLayout(
        value, onDecrease, onIncrease, decreaseDescription, increaseDescription, modifier, decreaseEnabled, increaseEnabled,
        shape = MaterialTheme.shapes.medium
    )
}

/**
 * The counter's layout, shared by every style - they differ only in the border's [shape], and in
 * [borderInset]: how far above and below the buttons the border is drawn in, to make the outline
 * look slimmer without shrinking the buttons' tap areas.
 */
@Composable
internal fun CounterLayout(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseDescription: String,
    increaseDescription: String,
    modifier: Modifier,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
    shape: Shape,
    borderInset: Dp = 0.dp
) {
    val borderColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = modifier.drawWithCache {
            val inset = borderInset.toPx()
            val strokeWidth = COUNTER_BORDER_WIDTH.toPx()
            val outlineSize = Size(size.width - strokeWidth, size.height - 2 * inset - strokeWidth)
            val outline = shape.createOutline(outlineSize, layoutDirection, this)
            onDrawBehind {
                translate(left = strokeWidth / 2, top = inset + strokeWidth / 2) {
                    drawOutline(outline, borderColor, style = Stroke(strokeWidth))
                }
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDecrease, enabled = decreaseEnabled, modifier = Modifier.size(COUNTER_BUTTON_SIZE)) {
            Icon(imageVector = Icons.Filled.Remove, contentDescription = decreaseDescription)
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(COUNTER_VALUE_WIDTH)
        )
        IconButton(onClick = onIncrease, enabled = increaseEnabled, modifier = Modifier.size(COUNTER_BUTTON_SIZE)) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = increaseDescription)
        }
    }
}
