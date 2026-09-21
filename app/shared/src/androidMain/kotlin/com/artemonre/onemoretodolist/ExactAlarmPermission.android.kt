package com.artemonre.onemoretodolist

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// SCHEDULE_EXACT_ALARM is only a distinct, user-grantable permission from Android 12 (API 31)
// onward - below that, exact alarms are freely available, so there's nothing to request.
@Composable
actual fun rememberExactAlarmPermissionState(): ExactAlarmPermissionState? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    val alarmManager = remember(context) { context.getSystemService<AlarmManager>() }
    var isGranted by remember(context) { mutableStateOf(alarmManager?.canScheduleExactAlarms() == true) }

    // The grant itself happens in system Settings, not in this app - refresh on resume so the
    // button/description update as soon as the user comes back, instead of staying stale until
    // the form is reopened.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, alarmManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = alarmManager?.canScheduleExactAlarms() == true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return remember(context, isGranted) {
        object : ExactAlarmPermissionState {
            override val isGranted = isGranted

            override fun request() {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }
}
