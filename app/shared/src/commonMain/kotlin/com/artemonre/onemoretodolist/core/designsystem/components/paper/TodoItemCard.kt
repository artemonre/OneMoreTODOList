package com.artemonre.onemoretodolist.core.designsystem.components.paper

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppCheckToggle
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CARD_SHAPE_RADIUS = 4.dp

// Kept above Material3's 1.dp ElevatedCard default so the press-flatten effect reads more clearly.
private val CARD_ELEVATION = 4.dp

// A fast tap can release before the shadow has had any frames to visibly animate down and back
// up. Holding the pressed visual for at least this long (measured from touch-down, only topping
// up whatever's left once release actually happens) guarantees it's always perceptible.
private val CARD_PRESS_MIN_VISIBLE_DURATION = 150.milliseconds

/**
 * A Paper-styled card for a single todo item: a checkbox, its text, and a formatted date,
 * with a press-driven elevation flatten for immediate tap feedback.
 *
 * The plain (non-onClick) ElevatedCard overload can't animate its own elevation param reactively,
 * so the press-driven shadow is drawn manually via Modifier.shadow() instead of Card's built-in
 * elevation system.
 */
@Composable
fun TodoItemCard(
    text: String,
    isDone: Boolean,
    formattedDate: String,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val cardShape = RoundedCornerShape(CARD_SHAPE_RADIUS)
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
        val textStyle = MaterialTheme.typography.bodyLarge
        val dateStyle = MaterialTheme.typography.labelSmall
        val textRowHeight = with(density) { textStyle.lineHeight.toDp() * 2 }
        val dateHeight = with(density) { dateStyle.lineHeight.toDp() }
        val dateSpacing = 4.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .height(textRowHeight + dateSpacing + dateHeight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckToggle(
                    checked = isDone,
                    onCheckedChange = { onToggleDone() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    style = textStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = formattedDate,
                style = dateStyle,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun TodoItemCardPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoItemCard(
            text = "Buy groceries",
            isDone = false,
            formattedDate = "Aug 24, 2026",
            onToggleDone = {},
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}
