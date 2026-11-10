package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloTagChip
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * A small, non-interactive colored label (e.g. a tag on a list row) for the current
 * [LocalUiStyle]: plain text on [containerColor], or Holo's outlined `# NAME`.
 */
@Composable
fun AppTagChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
                .background(color = containerColor, shape = MaterialTheme.shapes.small)
                .padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs)
        )
        UiStyleOption.Holo -> HoloTagChip(text, containerColor, contentColor, modifier)
    }
}
