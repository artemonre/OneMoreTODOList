package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalColorPalette
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A mascot drawing meant to sit over a card's top edge, as if peeking from behind it. Its square
 * canvas has empty space above the ears ([earsFraction] of its height), which is left out of the
 * layout so it doesn't push the card down. The card's edge goes [fenceFraction] down the canvas -
 * see [peekingMascotOverhang].
 */
@Composable
fun PeekingMascot(drawable: DrawableResource, size: Dp, earsFraction: Float, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val emptyTop = (placeable.height * earsFraction).roundToInt()
                layout(placeable.width, placeable.height - emptyTop) { placeable.place(0, -emptyTop) }
            }
            .size(size)
    )
}

/**
 * [monochrome] while dynamic color is on, [colored] otherwise - dynamic color can land on any hue,
 * which the colored drawings might clash with, while their grayscale versions go with anything.
 */
@Composable
fun mascotDrawable(colored: DrawableResource, monochrome: DrawableResource): DrawableResource =
    if (LocalColorPalette.current == null) monochrome else colored

/** How far a [PeekingMascot] sticks out above the card when the card's edge sits at [fenceFraction]. */
fun peekingMascotOverhang(size: Dp, earsFraction: Float, fenceFraction: Float): Dp =
    (fenceFraction - earsFraction) * size
