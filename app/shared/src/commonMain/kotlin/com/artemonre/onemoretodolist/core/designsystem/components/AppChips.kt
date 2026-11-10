package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialSuggestionChip
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

// Interactive one-off chips for the current LocalUiStyle. AppChipGroup covers single-choice rows,
// AppTagChip and AppTagButton cover tags on a form.

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
