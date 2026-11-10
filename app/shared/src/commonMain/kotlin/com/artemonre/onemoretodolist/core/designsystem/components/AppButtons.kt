package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloButton
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloTextButton
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/** A filled main-action button for the current [LocalUiStyle] - Material3's [Button] by default. */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material ->
            Button(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        UiStyleOption.Holo -> HoloButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

/**
 * A text button for the current [LocalUiStyle] - Material3's [TextButton] by default; Holo gives
 * it an outline. [emphasized] marks a dialog's main action: Holo draws it as an [AppButton], the
 * other styles ignore it.
 */
@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material ->
            TextButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        UiStyleOption.Holo -> if (emphasized) {
            HoloButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        } else {
            HoloTextButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        }
    }
}

/** A borderless icon-only button for the current [LocalUiStyle] - Material3's [IconButton] by default. */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    when (LocalUiStyle.current) {
        // No Holo icon button yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo ->
            IconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}
