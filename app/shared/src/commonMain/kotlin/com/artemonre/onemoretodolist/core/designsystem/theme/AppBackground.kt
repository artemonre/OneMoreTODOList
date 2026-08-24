package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.theme.domain.BackgroundOption

// Pattern and Image render as Solid today - no pattern/image asset exists yet.
// The seam is wired so a later task only needs to fill in a Brush/Image, not restructure this.
@Composable
fun AppBackground(
    style: BackgroundOption,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}
