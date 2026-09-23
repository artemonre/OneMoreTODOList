package com.artemonre.onemoretodolist.debug

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "debug_menu"
private const val KEY_EEA_UK_GEOGRAPHY = "eea_uk_geography"

// Debug-build-only settings, persisted so they survive the app restart Save triggers (see
// DebugMenuGate). RequestConsentIfEligible.android.kt (app:shared) reads the same prefs
// name/key directly - there's no release code path that could ever write true here.
class DebugPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isEeaUkGeographyEnabled: Boolean
        get() = prefs.getBoolean(KEY_EEA_UK_GEOGRAPHY, false)
        // commit (synchronous), not the default apply (async) - DebugMenuGate kills the process
        // immediately after this via Runtime.getRuntime().exit(0) to restart the app, which can
        // otherwise race apply()'s background disk write and lose the change entirely.
        set(value) = prefs.edit(commit = true) { putBoolean(KEY_EEA_UK_GEOGRAPHY, value) }
}
