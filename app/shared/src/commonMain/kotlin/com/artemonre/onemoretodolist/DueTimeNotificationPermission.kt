package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

interface DueTimeNotificationPermissionState {
    val isGranted: Boolean
    // allowSettingsRedirect gates whether this call is allowed to leave the app for the system
    // notification settings screen when no in-app prompt can be shown (permanently denied, or no
    // runtime prompt exists below Android 13) - true only for an explicit user action (the "Allow
    // notifications" button), never for an automatic call like turning "Due time" on, which should
    // silently do nothing rather than surprise-navigate the user out of the app.
    fun request(allowSettingsRedirect: Boolean = false)
}

// Returns a requester for posting due-time notifications, or null on any non-Android target (like
// ExactAlarmPermission/SpeechToText). Unlike ExactAlarmPermission's special-access Settings page,
// this is a genuine runtime-prompt permission on Android 13+ - below that there's nothing to
// request, isGranted just reflects the (always-on) app-level notification toggle.
@Composable
expect fun rememberDueTimeNotificationPermissionState(): DueTimeNotificationPermissionState?
