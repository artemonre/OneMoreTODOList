package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.artemonre.onemoretodolist.core.designsystem.theme.AccentSwatch
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAccentSwatches
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag

@Composable
internal fun TagColor.swatch(): AccentSwatch {
    val swatches = LocalAccentSwatches.current
    return when (this) {
        TagColor.Red -> swatches.red
        TagColor.Orange -> swatches.orange
        TagColor.Yellow -> swatches.yellow
        TagColor.Green -> swatches.green
        TagColor.Teal -> swatches.teal
        TagColor.Blue -> swatches.blue
        TagColor.Purple -> swatches.purple
        TagColor.Pink -> swatches.pink
    }
}

// A small, non-interactive colored label - how a tag reads on the todo card and in the detail
// dialog. The form uses interactive Material chips instead (see TodoFormTagsSection).
@Composable
fun TodoTagChip(tag: TodoTag, modifier: Modifier = Modifier) {
    val swatch = tag.color.swatch()
    Text(
        text = tag.name,
        style = MaterialTheme.typography.labelSmall,
        color = swatch.content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(color = swatch.container, shape = MaterialTheme.shapes.small)
            .padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs)
    )
}

@Preview
@Composable
private fun TodoTagChipPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoTagChip(TodoTag("Work", TagColor.Blue))
    }
}
