package com.artemonre.onemoretodolist.debug

import androidx.compose.runtime.Composable

// Release-build no-op - same public API as the real debug-only implementation (src/debug), so
// MainActivity can call it unconditionally. Nothing in that file (the screen, the volume-key
// counting, the debug consent path it enables) is compiled into a release build at all.
class DebugMenuGate {
    fun onVolumeKeyPressed() = Unit

    @Composable
    fun Overlay() = Unit
}
