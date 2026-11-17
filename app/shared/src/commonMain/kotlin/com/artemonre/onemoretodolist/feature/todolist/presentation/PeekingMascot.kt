package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalColorPalette
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A mascot drawing meant to sit over a card's top edge, as if peeking from behind it. The drawings
 * are cropped to their content, so [height] is the drawing's own height and its width follows from
 * its proportions. Where the card's edge crosses it is up to the caller - see [peekingMascotOverhang].
 */
@Composable
fun PeekingMascot(drawable: DrawableResource, height: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        modifier = modifier.height(height)
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
fun peekingMascotOverhang(height: Dp, fenceFraction: Float): Dp = fenceFraction * height
