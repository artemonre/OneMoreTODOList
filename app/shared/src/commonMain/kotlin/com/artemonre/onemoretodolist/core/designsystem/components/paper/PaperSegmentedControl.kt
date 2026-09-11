package com.artemonre.onemoretodolist.core.designsystem.components.paper

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// Matches ListItemCard/Card's own elevation, so the control reads as part of the same Paper
// language.
private val CONTROL_ELEVATION = 6.dp

/**
 * [PaperListItemCardShape] with a full-height rectangular bite taken out of it at each index in
 * [notchIndices], out of [segmentCount] equal-width slices. Used only for the shadow (see
 * [PaperSegmentedControl]) - shadow() draws its blur outside the given shape's silhouette, so
 * shadowing this notched outline instead of the plain pill makes the shadow wrap around a
 * segment's gap, reading as an actual recess rather than just a color change on an otherwise
 * uniformly "floating" bar. More than one index can be notched at once, for the moment a
 * different segment is pressed while the previously selected one hasn't visually settled yet.
 */
private class NotchedShadowShape(
    private val notchIndices: Set<Int>,
    private val segmentCount: Int
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val baseOutline = PaperListItemCardShape.createOutline(size, layoutDirection, density)
        val basePath = Path().apply {
            when (baseOutline) {
                is Outline.Rounded -> addRoundRect(baseOutline.roundRect)
                is Outline.Rectangle -> addRect(baseOutline.rect)
                is Outline.Generic -> addPath(baseOutline.path)
            }
        }
        if (notchIndices.isEmpty()) return Outline.Generic(basePath)

        val segmentWidth = size.width / segmentCount
        val notchesPath = Path().apply {
            notchIndices.forEach { index ->
                addRect(
                    Rect(
                        left = segmentWidth * index,
                        top = 0f,
                        right = segmentWidth * (index + 1),
                        bottom = size.height
                    )
                )
            }
        }
        val notchedPath = Path()
        notchedPath.op(basePath, notchesPath, PathOperation.Difference)
        return Outline.Generic(notchedPath)
    }
}

/**
 * A Paper-styled single-choice segmented control: one solid bar - rounded only at its far
 * left/right ends, split into equal-width segments by thin dividers rather than gaps, so it reads
 * as "one piece" instead of a group of separate buttons.
 *
 * No ripple - pressing a segment previews the selected look immediately (background color, no
 * elevation) instead of waiting for the click to actually commit, same as flicking a real switch.
 * Both the segment being pressed and the still-officially-selected one (if different) stay flat
 * without elevation for the duration of the press, so releasing doesn't cause one to visibly pop
 * back up before the other settles down.
 *
 * A click also optimistically marks its own segment flat right away (rather than waiting for
 * [selectedOption] to actually reflect the change) - [onOptionSelected] usually round-trips
 * through a ViewModel/persistence layer that takes a few frames to come back, and without this,
 * isPressed drops to false on release before that round-trip completes, so the segment would
 * elevate again for a moment before settling once selectedOption finally catches up.
 */
@Composable
fun <T> PaperSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val selectedIndex = options.indexOf(selectedOption)
    var optimisticIndex by remember { mutableStateOf<Int?>(null) }
    // Drop the optimistic override once the real selection catches up to it (or changes for any
    // other reason) - from then on the plain isSelected check below is already correct on its own.
    LaunchedEffect(selectedIndex) { optimisticIndex = null }

    val interactionSources = remember(options.size) {
        List(options.size) { MutableInteractionSource() }
    }
    val flatIndices = buildSet {
        add(optimisticIndex ?: selectedIndex)
        interactionSources.forEachIndexed { index, interactionSource ->
            if (interactionSource.collectIsPressedAsState().value) add(index)
        }
    }
    val shadowShape = remember(flatIndices, options.size) {
        NotchedShadowShape(notchIndices = flatIndices, segmentCount = options.size)
    }

    Surface(
        // clip = false - shadow()'s default clip (elevation > 0.dp) would clip the actual content
        // to shadowShape too, cutting a real hole through the flat segment(s) instead of just
        // notching the shadow. The unnotched PaperListItemCardShape below still clips/fills as
        // normal.
        modifier = modifier.shadow(elevation = CONTROL_ELEVATION, shape = shadowShape, clip = false),
        shape = PaperListItemCardShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
        ) {
            options.forEachIndexed { index, option ->
                val isFlat = index in flatIndices
                val dividerColor = MaterialTheme.colorScheme.outlineVariant
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = option == selectedOption,
                            interactionSource = interactionSources[index],
                            indication = null,
                            onClick = {
                                optimisticIndex = index
                                onOptionSelected(option)
                            },
                            role = Role.RadioButton
                        )
                        .background(
                            if (isFlat) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
                        )
                        .then(
                            if (index != options.lastIndex) {
                                Modifier.drawBehind {
                                    drawLine(
                                        color = dividerColor,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = label(option), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 88)
@Composable
private fun PaperSegmentedControlPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        PaperSegmentedControl(
            options = listOf("System", "Light", "Dark"),
            selectedOption = "Light",
            onOptionSelected = {},
            label = { it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
