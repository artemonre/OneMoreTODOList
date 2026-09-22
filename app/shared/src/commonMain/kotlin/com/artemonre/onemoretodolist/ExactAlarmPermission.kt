package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

interface ExactAlarmPermissionState {
    val isGranted: Boolean
    fun request()
}

// Returns a requester for scheduling exact alarms, or null where this isn't a distinct grantable
// permission (pre-Android 12, or any non-Android target) - callers should hide the "Grant
// permission" affordance when this is null.
@Composable
expect fun rememberExactAlarmPermissionState(): ExactAlarmPermissionState?
