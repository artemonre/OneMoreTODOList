package com.artemonre.onemoretodolist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private const val PREFS_NAME = "due_time_notification_permission"
private const val KEY_HAS_REQUESTED = "has_requested"

@Composable
actual fun rememberDueTimeNotificationPermissionState(): DueTimeNotificationPermissionState? {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val prefs = remember(context) { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    // Version-agnostic: covers both the pre-33 app-level notification toggle and the 33+ runtime
    // permission with a single check.
    var isGranted by remember(context) { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    // Distinguishes "never asked" from "permanently denied" for shouldShowRequestPermissionRationale
    // below, which returns false for both - persisted (not just remembered) because the permission
    // can have been permanently denied in a past app session, before this composable ever ran.
    var hasRequestedBefore by remember(prefs) { mutableStateOf(prefs.getBoolean(KEY_HAS_REQUESTED, false)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isGranted = NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> isGranted = granted }

    return remember(context, activity, isGranted, hasRequestedBefore) {
        object : DueTimeNotificationPermissionState {
            override val isGranted = isGranted

            override fun request(allowSettingsRedirect: Boolean) {
                if (isGranted) return
                // No runtime prompt at all below 33 - the only way to flip the app-level toggle is
                // the system's per-app notification settings screen.
                val canShowSystemPrompt = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    (!hasRequestedBefore ||
                        activity?.let {
                            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
                        } == true)
                if (canShowSystemPrompt) {
                    prefs.edit { putBoolean(KEY_HAS_REQUESTED, true) }
                    hasRequestedBefore = true
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if (allowSettingsRedirect) {
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    )
                }
            }
        }
    }
}
