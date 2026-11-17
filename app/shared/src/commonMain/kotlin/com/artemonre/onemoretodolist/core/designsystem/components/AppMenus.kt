package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/** A pop-up menu anchored to its parent for the current [LocalUiStyle] - Material3's [DropdownMenu] by default. */
@Composable
fun AppDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    when (LocalUiStyle.current) {
        // No Holo menu yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo ->
            DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier, content = content)
    }
}

/** One row of an [AppDropdownMenu] - Material3's [DropdownMenuItem] by default. */
@Composable
fun AppDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    when (LocalUiStyle.current) {
        // No Holo menu yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo ->
            DropdownMenuItem(text = text, onClick = onClick, modifier = modifier, trailingIcon = trailingIcon)
    }
}
