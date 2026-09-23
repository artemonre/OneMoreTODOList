package com.artemonre.onemoretodolist.core.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// A small horizontal banner ad. Renders nothing and takes up no space on any non-Android target -
// there's no ad surface on desktop/web, only the Android gateway currently monetizes.
@Composable
expect fun AdBanner(modifier: Modifier = Modifier)
