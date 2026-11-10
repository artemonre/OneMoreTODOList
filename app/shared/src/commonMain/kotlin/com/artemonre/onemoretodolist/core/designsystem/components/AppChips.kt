package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAssistChip
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialInputChip
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialSuggestionChip
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

// Interactive chips for the current LocalUiStyle. AppChipGroup covers single-choice rows; these are
// the one-off chips: a colored value the user put in (input), an action (assist) and a colored
// pick-me (suggestion).

/**
 * A colored chip for something the user added, like a tag on a form. [trailingIcon], when set, is
 * shown after [text] - e.g. a remove cross, described by [trailingIconDescription].
 */
@Composable
fun AppInputChip(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null
) {
    when (LocalUiStyle.current) {
        // No Holo input chip yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo -> MaterialInputChip(
            selected = false,
            onClick = onClick,
            label = { Text(text) },
            modifier = modifier,
            trailingIcon = trailingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = trailingIconDescription,
                        modifier = Modifier.size(InputChipDefaults.IconSize)
                    )
                }
            },
            colors = InputChipDefaults.inputChipColors(
                containerColor = containerColor,
                labelColor = contentColor,
                trailingIconColor = contentColor
            ),
            border = null
        )
    }
}

/** A neutral chip that runs an action, like "Add tag". [leadingIcon] is decorative. */
@Composable
fun AppAssistChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    when (LocalUiStyle.current) {
        // No Holo assist chip yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo -> MaterialAssistChip(
            onClick = onClick,
            label = { Text(text) },
            modifier = modifier,
            leadingIcon = leadingIcon?.let { icon -> { Icon(imageVector = icon, contentDescription = null) } }
        )
    }
}

/** A colored chip offering a ready-made pick, like an existing tag to reuse. */
@Composable
fun AppSuggestionChip(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        // No Holo suggestion chip yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo -> MaterialSuggestionChip(
            onClick = onClick,
            label = { Text(text) },
            modifier = modifier,
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = containerColor,
                labelColor = contentColor
            ),
            border = null
        )
    }
}
