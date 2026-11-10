package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.artemonre.onemoretodolist.core.designsystem.components.AppTagChip
import com.artemonre.onemoretodolist.core.designsystem.theme.AccentSwatch
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAccentSwatches
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag

@Composable
internal fun TagColor.swatch(): AccentSwatch = LocalAccentSwatches.current.slots[ordinal]

// A small, non-interactive colored label - how a tag reads on the todo card and in the detail
// dialog. The form uses interactive Material chips instead (see TodoFormTagsSection).
@Composable
fun TodoTagChip(tag: TodoTag, modifier: Modifier = Modifier) {
    val swatch = tag.color.swatch()
    AppTagChip(text = tag.name, containerColor = swatch.container, contentColor = swatch.content, modifier = modifier)
}

@Preview
@Composable
private fun TodoTagChipPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoTagChip(TodoTag("Work", TagColor.Color6))
    }
}
