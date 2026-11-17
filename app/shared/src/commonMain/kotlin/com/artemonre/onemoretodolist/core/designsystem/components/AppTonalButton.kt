package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloTonalButton
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * A secondary, filled button for the current [LocalUiStyle] - Material3's [FilledTonalButton],
 * or a selected-chip pill on Holo.
 */
@Composable
fun AppTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material ->
            FilledTonalButton(onClick = onClick, modifier = modifier, contentPadding = contentPadding, content = content)
        UiStyleOption.Holo -> HoloTonalButton(onClick = onClick, modifier = modifier, contentPadding = contentPadding, content = content)
    }
}
