package com.artemonre.onemoretodolist.core.ads

import androidx.compose.runtime.Composable

// Call the returned lambda after a todo is created or completed - it shows a preloaded
// interstitial if one is ready and none has been shown yet today, and silently does nothing
// otherwise (no ad ready, already shown today, or any non-Android target). Safe to call
// unconditionally; the once-a-day gating lives entirely on the Android side.
@Composable
expect fun rememberInterstitialAdTrigger(): () -> Unit
