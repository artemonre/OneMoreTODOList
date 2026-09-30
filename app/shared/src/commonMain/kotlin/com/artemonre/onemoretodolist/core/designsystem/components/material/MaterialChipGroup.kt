package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

/**
 * A single-choice control using a row of stock Material3 [FilterChip]s - default shape, colors,
 * and border all from `FilterChipDefaults`, no customizations. Wraps onto extra lines by default;
 * [singleLine] keeps every chip on one line and scrolls it horizontally instead.
 */
@Composable
fun <T> MaterialChipGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false
) {
    val chips: @Composable () -> Unit = {
        options.forEach { option ->
            FilterChip(
                selected = option == selectedOption,
                onClick = { onOptionSelected(option) },
                label = { Text(label(option), maxLines = 1) }
            )
        }
    }
    if (singleLine) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.horizontalScroll(rememberScrollState())
        ) {
            chips()
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
