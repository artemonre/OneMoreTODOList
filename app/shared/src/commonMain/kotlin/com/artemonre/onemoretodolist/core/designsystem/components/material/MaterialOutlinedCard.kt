package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing

/**
 * A stock Material3 [OutlinedCard], in [shape] (Material's own by default), with an optional
 * [footer]: a bottom strip on the screen's background color, edge to edge, that resizes smoothly
 * as its text changes.
 */
@Composable
fun MaterialOutlinedCard(
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    shape: Shape = CardDefaults.outlinedShape,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(modifier = modifier, shape = shape) {
        content()
        if (footer != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .animateContentSize()
                    .padding(AppSpacing.m),
                content = footer
            )
        }
    }
}
