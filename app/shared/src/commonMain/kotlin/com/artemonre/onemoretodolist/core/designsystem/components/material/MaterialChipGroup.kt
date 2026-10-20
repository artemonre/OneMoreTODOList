package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// One step down from FilterChipDefaults.ContentPadding (8dp horizontal).
private val COMPACT_CHIP_CONTENT_PADDING = PaddingValues(horizontal = AppSpacing.xs)

/**
 * A single-choice control using a row of stock Material3 [MaterialFilterChip]s - default shape, colors,
 * and border all from `FilterChipDefaults`, no customizations. Wraps onto extra lines by default;
 * [singleLine] keeps every chip on one line and scrolls it horizontally instead. [compact] uses a
 * smaller label and one spacing step less horizontal padding.
 */
@Composable
fun <T> MaterialChipGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    compact: Boolean = false
) {
    val compactLabelStyle = MaterialTheme.typography.bodySmall
    val contentPadding = if (compact) COMPACT_CHIP_CONTENT_PADDING else FilterChipDefaults.ContentPadding
    val chips: @Composable () -> Unit = {
        options.forEach { option ->
            MaterialFilterChip(
                selected = option == selectedOption,
                onClick = { onOptionSelected(option) },
                label = {
                    // LocalTextStyle here is the chip's own label style, provided by FilterChip.
                    Text(label(option), maxLines = 1, style = if (compact) compactLabelStyle else LocalTextStyle.current)
                },
                contentPadding = contentPadding
            )
        }
    }
    if (singleLine) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            modifier = modifier.horizontalScroll(rememberScrollState())
        ) {
            chips()
        }
    } else {
        // No extra gap between wrapped rows - each chip's 48dp touch target already leaves 8dp
        // above and below its 32dp body.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.s),
            modifier = modifier
        ) {
            chips()
        }
    }
}

@Preview(widthDp = 300, heightDp = 88)
@Composable
private fun MaterialChipGroupPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        MaterialChipGroup(
            options = listOf("Material", "Paper"),
            selectedOption = "Material",
            onOptionSelected = {},
            label = { it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
