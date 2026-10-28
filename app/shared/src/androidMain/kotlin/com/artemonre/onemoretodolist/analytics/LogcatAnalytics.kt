package com.artemonre.onemoretodolist.analytics

import android.util.Log

private const val TAG = "Analytics"

// Debug builds' stand-in for PostHogAnalytics: every event goes to logcat instead, shaped the way
// PostHog would get it (null properties left out), so `adb logcat -s Analytics` shows exactly what a
// release build would send.
object LogcatAnalytics : Analytics {
    override fun capture(event: AnalyticsEvent) {
        val properties = event.properties.filterValues { it != null }
        Log.d(TAG, if (properties.isEmpty()) event.name else "${event.name} $properties")
    }
}
