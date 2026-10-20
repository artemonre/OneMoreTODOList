package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.ui.unit.dp

// Spacing scale for the padding we own - screen edges, gaps between unrelated groups, our own
// cards and sheets. Material3 has no theme-level spacing tokens; padding inside its components
// comes from each component's own defaults and doesn't need anything from here.
object AppSpacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
}
