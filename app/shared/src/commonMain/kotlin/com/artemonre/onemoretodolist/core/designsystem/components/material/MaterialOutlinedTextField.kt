package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.isUnspecified
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing

private val STOCK_CONTENT_PADDING = OutlinedTextFieldDefaults.contentPadding()
private val COMPACT_CONTENT_PADDING = PaddingValues(AppSpacing.s)

// The stock 56dp minimum fits exactly one line plus 16dp top and bottom - shrunk by however much
// the compact padding takes off, so the smaller padding actually makes the field shorter.
private val COMPACT_MIN_HEIGHT = OutlinedTextFieldDefaults.MinHeight -
    (STOCK_CONTENT_PADDING.calculateTopPadding() - AppSpacing.s) * 2

/**
 * A stock Material3 [OutlinedTextField] (the [TextFieldState] overload) - default shape, colors,
 * and label behavior. [compact] shrinks the inner padding from 16dp to 8dp, and the minimum
 * height along with it.
 */
@Composable
fun MaterialOutlinedTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: KeyboardActionHandler? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
    compact: Boolean = false
) {
    val contentPadding = when {
        !compact -> STOCK_CONTENT_PADDING
        trailingIcon != null && lineLimits != TextFieldLineLimits.SingleLine -> compactPaddingBesideIcon()
        else -> COMPACT_CONTENT_PADDING
    }
    OutlinedTextField(
        state = state,
        // A non-zero min constraint here takes precedence over the stock 56dp default minimum.
        modifier = if (compact) modifier.heightIn(min = COMPACT_MIN_HEIGHT) else modifier,
        readOnly = readOnly,
        label = label?.let { { it() } },
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        lineLimits = lineLimits,
        contentPadding = contentPadding
    )
}

// Multi-line text is pinned to the top padding instead of centered, and a trailing icon button
// keeps the field at least its touch-target height (48dp). With plain 8dp padding one line would sit
// off-center, so the vertical padding grows until one line fills exactly that height.
@Composable
private fun compactPaddingBesideIcon(): PaddingValues {
    val lineHeight = LocalTextStyle.current.lineHeight
    val touchTarget = LocalMinimumInteractiveComponentSize.current
    if (lineHeight.isUnspecified || touchTarget.isUnspecified) return COMPACT_CONTENT_PADDING
    val vertical = with(LocalDensity.current) { (touchTarget - lineHeight.toDp()) / 2 }
    return PaddingValues(horizontal = AppSpacing.s, vertical = vertical.coerceAtLeast(AppSpacing.s))
}
