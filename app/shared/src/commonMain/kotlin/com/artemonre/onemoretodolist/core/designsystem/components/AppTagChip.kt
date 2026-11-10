package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloTagButton
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloTagChip
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

private val TAG_ICON_SIZE = 12.dp

// Tappable tags (on a form) are drawn bigger than read-only ones, so they're easy to hit.
private val TAPPABLE_TAG_HEIGHT = 32.dp
private val TAPPABLE_TAG_ICON_SIZE = 16.dp

/**
 * A small colored tag label for the current [LocalUiStyle]: plain text on [containerColor], or
 * Holo's outlined `# NAME`. Read-only on a list row; with [onClick] (and e.g. a remove cross as
 * [trailingIcon], described by [trailingIconDescription]) it's the same label made tappable and
 * drawn bigger - 32dp tall, larger text and icons - with a full-size touch area.
 */
@Composable
fun AppTagChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> TagChipLayout(
            containerColor = containerColor,
            contentColor = contentColor,
            border = null,
            onClick = onClick,
            trailingIcon = trailingIcon,
            trailingIconDescription = trailingIconDescription,
            modifier = modifier
        ) {
            Text(text = text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        UiStyleOption.Holo -> HoloTagChip(
            text = text,
            containerColor = containerColor,
            contentColor = contentColor,
            modifier = modifier,
            onClick = onClick,
            trailingIcon = trailingIcon,
            trailingIconDescription = trailingIconDescription
        )
    }
}

/**
 * A neutral action sized like a tappable [AppTagChip], to sit among tags - like "Add tag". Uses Material's
 * assist-chip roles: outlined in `outlineVariant`, `onSurface` text, a `primary` [leadingIcon].
 */
@Composable
fun AppTagButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> TagChipLayout(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            onClick = onClick,
            leadingIcon = leadingIcon,
            leadingIconColor = MaterialTheme.colorScheme.primary,
            modifier = modifier
        ) {
            Text(text = text, maxLines = 1)
        }
        UiStyleOption.Holo -> HoloTagButton(text = text, onClick = onClick, modifier = modifier, leadingIcon = leadingIcon)
    }
}

/**
 * The shared frame of tag chips and tag buttons: a small rounded label with optional icons on
 * either side. When [onClick] is set the whole label is one button: 32dp tall with [label] in
 * `labelLarge` and 16dp icons, inside a 48dp touch area. Otherwise it's a 24dp `labelSmall` label.
 * [label] picks up the text style and [contentColor] from here.
 */
@Composable
internal fun TagChipLayout(
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    leadingIconColor: Color = contentColor,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null,
    label: @Composable () -> Unit
) {
    val shape = MaterialTheme.shapes.small
    val tappable = onClick != null
    val iconSize = if (tappable) TAPPABLE_TAG_ICON_SIZE else TAG_ICON_SIZE
    val textStyle = if (tappable) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall
    Row(
        modifier = modifier
            .then(if (tappable) Modifier.minimumInteractiveComponentSize() else Modifier)
            .clip(shape)
            .background(containerColor)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(onClick = onClick, role = Role.Button)
                        .defaultMinSize(minHeight = TAPPABLE_TAG_HEIGHT)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, tint = leadingIconColor, modifier = Modifier.size(iconSize))
        }
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalTextStyle provides textStyle,
            content = label
        )
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = trailingIconDescription,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
