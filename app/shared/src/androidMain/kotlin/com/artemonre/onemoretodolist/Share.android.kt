package com.artemonre.onemoretodolist

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

// Pinned via EXTRA_INITIAL_INTENTS, in this order, ahead of the system's usage-ranked list -
// only the ones actually installed (and able to handle text/plain) are shown.
private val PRIORITY_SHARE_PACKAGES = listOf(
    "com.whatsapp",
    "org.telegram.messenger",
    "org.thoughtcrime.securesms",
    "com.google.android.apps.messaging",
    "com.google.android.gm"
)

@Composable
actual fun rememberNativeShareLauncher(): ((String) -> Unit)? {
    val context = LocalContext.current
    return remember(context) {
        { text: String ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val packageManager = context.packageManager
            val priorityIntents = PRIORITY_SHARE_PACKAGES.mapNotNull { packageName ->
                resolvedSendIntentOrNull(packageManager, sendIntent, packageName)
            }.toTypedArray()

            val chooserIntent = Intent.createChooser(sendIntent, null).apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, priorityIntents)
            }
            context.startActivity(chooserIntent)
        }
    }
}

private fun resolvedSendIntentOrNull(
    packageManager: PackageManager,
    sendIntent: Intent,
    packageName: String
): Intent? {
    val explicitIntent = Intent(sendIntent).setPackage(packageName)
    val resolvesToAnActivity = packageManager.queryIntentActivities(explicitIntent, 0).isNotEmpty()
    return explicitIntent.takeIf { resolvesToAnActivity }
}
