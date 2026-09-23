package com.artemonre.onemoretodolist.debug

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

private const val TAPS_TO_OPEN = 3

// Debug-build-only: opens a hidden debug menu after three volume-key presses (up or down, either
// one counts). Real implementation lives only in this source set - src/release has a no-op stub
// with the same API, so none of this (the screen, the key-counting, the forced-EEA debug ad
// consent path it enables) is even compiled into a release build, not just disabled at runtime.
class DebugMenuGate {
    private var tapCount = 0
    private var onOpen: (() -> Unit)? = null

    fun onVolumeKeyPressed() {
        tapCount++
        if (tapCount >= TAPS_TO_OPEN) {
            tapCount = 0
            onOpen?.invoke()
        }
    }

    @Composable
    fun Overlay() {
        val context = LocalContext.current
        val debugPreferences = remember { DebugPreferences(context) }
        var isOpen by remember { mutableStateOf(false) }
        onOpen = { isOpen = true }

        if (isOpen) {
            DebugMenuScreen(
                initialEeaUkGeographyEnabled = debugPreferences.isEeaUkGeographyEnabled,
                onSave = { eeaUkGeographyEnabled ->
                    debugPreferences.isEeaUkGeographyEnabled = eeaUkGeographyEnabled
                    isOpen = false
                    restartApp(context)
                },
                onDismiss = { isOpen = false }
            )
        }
    }
}

// A full process restart (not just recreating the Activity) so every singleton set up at process
// start - Koin, MobileAds.initialize(), the consent check this debug setting actually affects -
// runs fresh with the new setting applied.
private fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    val restartIntent = Intent.makeRestartActivityTask(intent.component)
    context.startActivity(restartIntent)
    Runtime.getRuntime().exit(0)
}
