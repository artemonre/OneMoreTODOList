package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloTextField
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialOutlinedTextField
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * A labeled, bordered text field for the current [LocalUiStyle] - Material3's outlined field by
 * default. [compact] shrinks the inner padding for tight spots like dialogs.
 */
@Composable
fun AppTextField(
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
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialOutlinedTextField(
            state, modifier, readOnly, label, placeholder, trailingIcon, keyboardOptions, onKeyboardAction, lineLimits, compact
        )
        UiStyleOption.Holo -> HoloTextField(
            state, modifier, readOnly, label, placeholder, trailingIcon, keyboardOptions, onKeyboardAction, lineLimits, compact
        )
    }
}
