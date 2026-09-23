package com.artemonre.onemoretodolist.core.ads

import android.content.Context
import com.google.android.ump.ConsentDebugSettings

// Always present (a harmless no-op by default, including in release) - only gateway/todoList's
// src/debug overrides this binding with a real implementation, so every actual debug-only detail
// (which SharedPreferences flag, the test-device hash computation, forcing EEA geography) stays
// entirely out of this file and everything else in app:shared's regular (always-compiled) source
// set. See androidAdsModule for the default binding and gateway/todoList's debug module for the
// override.
fun interface DebugConsentSettingsProvider {
    fun get(context: Context): ConsentDebugSettings?
}
