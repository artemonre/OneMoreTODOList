package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

private class KeyboardTapState {
    var tapKeepsKeyboard = false
}

private val LocalKeyboardTapState = staticCompositionLocalOf<KeyboardTapState?> { null }

/**
 * Clears focus - closing the keyboard - when a button, checkbox or other clickable inside [content]
 * handles a tap. Taps on empty background, on a field marked [keepsKeyboardOnTap], and drags
 * (scrolling) are left alone.
 */
@Composable
fun DismissKeyboardOnTap(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember { KeyboardTapState() }
    val focusManager = LocalFocusManager.current
    Box(
        modifier = modifier.pointerInput(focusManager) {
            awaitEachGesture {
                // Final pass, so children have already handled the gesture - we only observe it,
                // never consume. A clickable consumes the tap; empty background leaves it unconsumed.
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
                var isTap = true
                var isHandledByChild = down.isConsumed
                while (true) {
                    val change = awaitPointerEvent(PointerEventPass.Final).changes
                        .firstOrNull { it.id == down.id } ?: break
                    if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                        isTap = false
                    }
                    if (change.isConsumed) isHandledByChild = true
                    if (!change.pressed) break
                }
                if (isTap && isHandledByChild && !state.tapKeepsKeyboard) focusManager.clearFocus()
                state.tapKeepsKeyboard = false
            }
        }
    ) {
        CompositionLocalProvider(LocalKeyboardTapState provides state, content = content)
    }
}

/**
 * Marks a text field whose taps shouldn't close the keyboard inside [DismissKeyboardOnTap]. A no-op
 * anywhere else.
 */
@Composable
fun Modifier.keepsKeyboardOnTap(): Modifier {
    val state = LocalKeyboardTapState.current ?: return this
    return pointerInput(state) {
        awaitEachGesture {
            // Initial pass on down - runs before DismissKeyboardOnTap's Final-pass handling of the
            // same gesture's up.
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            state.tapKeepsKeyboard = true
        }
    }
}
