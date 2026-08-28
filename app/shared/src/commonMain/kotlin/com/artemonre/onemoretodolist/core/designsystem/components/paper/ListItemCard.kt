package com.artemonre.onemoretodolist.core.designsystem.components.paper

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CARD_SHAPE_RADIUS = 4.dp

// Exposed so callers that draw behind/around the card (e.g. a swipe-reveal background) can match
// its corners exactly - see AppListItemCardShape.
val PaperListItemCardShape: Shape = RoundedCornerShape(CARD_SHAPE_RADIUS)

// Kept above Material3's 1.dp ElevatedCard default so the press-flatten effect reads more clearly.
private val CARD_ELEVATION = 4.dp

// A fast tap can release before the shadow has had any frames to visibly animate down and back
// up. Holding the pressed visual for at least this long (measured from touch-down, only topping
// up whatever's left once release actually happens) guarantees it's always perceptible.
private val CARD_PRESS_MIN_VISIBLE_DURATION = 150.milliseconds

/**
 * A Paper-styled card: a press-driven elevation flatten for immediate tap feedback around
 * arbitrary [content]. Domain-agnostic - any feature can use it for a clickable list row without
 * this module knowing what that row represents.
 *
 * The plain (non-onClick) ElevatedCard overload can't animate its own elevation param reactively,
 * so the press-driven shadow is drawn manually via Modifier.shadow() instead of Card's built-in
 * elevation system.
 */
@Composable
fun ListItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val cardShape = PaperListItemCardShape
    var isCardPressed by remember { mutableStateOf(false) }
    val cardElevation by animateDpAsState(if (isCardPressed) 0.dp else CARD_ELEVATION)

    ElevatedCard(
        modifier = modifier
            .shadow(elevation = cardElevation, shape = cardShape)
            // Raw pointer down/up drives the visual only, immediately and independent of any
            // scroll-vs-tap disambiguation. clickable (below) still owns onClick and all of
            // Compose's built-in tap/scroll/drag safety for the actual action - inside a
            // LazyColumn, clickable intentionally delays/suppresses its own Press interaction
            // until it's sure a gesture isn't the start of a scroll, so a fast tap never reaches
            // clickable's interactionSource in time to drive a press effect from it.
            .pointerInput(Unit) {
                // awaitEachGesture runs in a restricted suspend scope that can't call
                // kotlinx.coroutines.delay directly, so the minimum-visible-duration wait is
                // done in a plain coroutine launched off it instead.
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    val pressStartMark = TimeSource.Monotonic.markNow()
                    isCardPressed = true
                    waitForUpOrCancellation()
                    coroutineScope.launch {
                        val remaining = CARD_PRESS_MIN_VISIBLE_DURATION - pressStartMark.elapsedNow()
                        if (remaining > Duration.ZERO) delay(remaining)
                        isCardPressed = false
                    }
                }
            }
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            ),
        shape = cardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}
