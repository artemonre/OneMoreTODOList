package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloButton
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloTextButton
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
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
 * other styles ignore it. [leadingIcon], when set, goes before [content] at Material's in-button
 * icon size, with the narrower start padding Material uses beside an icon.
 */
@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    leadingIcon: ImageVector? = null,
    content: @Composable RowScope.() -> Unit
) {
    val iconAndContent: @Composable RowScope.() -> Unit = if (leadingIcon == null) {
        content
    } else {
        {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            content()
        }
    }
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            contentPadding = if (leadingIcon == null) ButtonDefaults.TextButtonContentPadding else ButtonDefaults.TextButtonWithIconContentPadding,
            content = iconAndContent
        )
        UiStyleOption.Holo -> if (emphasized) {
            val padding = if (leadingIcon == null) {
                PaddingValues(horizontal = AppSpacing.l)
            } else {
                PaddingValues(start = AppSpacing.m, end = AppSpacing.l)
            }
            HoloButton(onClick = onClick, modifier = modifier, enabled = enabled, contentPadding = padding, content = iconAndContent)
        } else {
            // Holo's outlined secondary buttons are compact: 8dp each side, icon or not.
            HoloTextButton(onClick = onClick, modifier = modifier, enabled = enabled, content = iconAndContent)
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
