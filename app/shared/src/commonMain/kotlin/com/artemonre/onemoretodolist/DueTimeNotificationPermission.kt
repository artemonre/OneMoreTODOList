package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

interface DueTimeNotificationPermissionState {
    val isGranted: Boolean
    fun request()
}

// Returns a requester for posting due-time notifications, or null on any non-Android target (like
// ExactAlarmPermission/SpeechToText). Unlike ExactAlarmPermission's special-access Settings page,
// this is a genuine runtime-prompt permission on Android 13+ - below that there's nothing to
// request, isGranted just reflects the (always-on) app-level notification toggle.
@Composable
expect fun rememberDueTimeNotificationPermissionState(): DueTimeNotificationPermissionState?
