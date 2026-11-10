package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloDivider
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloOutlinedButton
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloSectionTitle
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloSwitch
import com.artemonre.onemoretodolist.core.designsystem.components.holo.holoSupportingTextStyle
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * A section's title, as a heading. Holo puts the section's [number] before it (`01 // Title`);
 * Material shows the title alone.
 */
@Composable
fun AppSectionTitle(
    title: String,
    number: Int?,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.semantics { heading() }
        )
        UiStyleOption.Holo -> HoloSectionTitle(title, number, modifier)
    }
}

/** An on/off switch for the current [LocalUiStyle]. */
@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material ->
            Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier, enabled = enabled)
        UiStyleOption.Holo -> HoloSwitch(checked, onCheckedChange, modifier, enabled)
    }
}

/** An outlined button with a leading [icon] for the current [LocalUiStyle]. */
@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            contentPadding = contentPadding,
            modifier = modifier
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(text)
        }
        UiStyleOption.Holo -> HoloOutlinedButton(onClick, text, icon, modifier, enabled, contentPadding)
    }
}

/**
 * A thin line between the parts of a section. Only Holo draws one; Material separates
 * the parts with spacing alone.
 */
@Composable
fun AppSectionDivider(modifier: Modifier = Modifier) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> Unit
        UiStyleOption.Holo -> HoloDivider(modifier)
    }
}

/** The style for small supporting text under a setting or a control (descriptions, notes). */
@Composable
fun appSupportingTextStyle(): TextStyle = when (LocalUiStyle.current) {
    UiStyleOption.Material -> MaterialTheme.typography.bodySmall
    UiStyleOption.Holo -> holoSupportingTextStyle()
}
