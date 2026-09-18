package com.artemonre.onemoretodolist

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallException
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import org.koin.core.context.GlobalContext

// Only the Immediate flow is implemented for now - Flexible (background download, resumable,
// non-blocking) is a possible follow-up but isn't needed yet.
private const val MANDATORY_UPDATE_MIN_PRIORITY = 5

// Reads the Context Koin already binds in TodoListApplication.onCreate() (androidContext(...))
// instead of threading Context through this expect/actual's signature, same pattern as
// AppVersion.android.kt.
//
// requestAppUpdateInfo() throws InstallException on any Play Core failure - most notably
// ERROR_APP_NOT_OWNED, which fires whenever the app wasn't installed via Play (Play's own
// Pre-launch report test devices hit this on every release, and it also fires for anyone
// sideloading a build). Left uncaught, this crashed the whole app on startup, since this check
// runs inside MandatoryAppUpdateGate, which wraps all of App(). Any failure here means we can't
// determine update status, so fail safe: no update.
actual suspend fun isAppUpdateAvailable(): Boolean {
    val context = GlobalContext.get().get<Context>()
    return try {
        val appUpdateInfo = AppUpdateManagerFactory.create(context).requestAppUpdateInfo()
        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
    } catch (e: InstallException) {
        false
    }
}

// Priority 5 (set via `--update-priority` on `publishBundle`, see
// gateway/todoList/build.gradle.kts) means the update is required - the app blocks on it at
// startup via MandatoryAppUpdateGate instead of leaving it to the user in Settings.
actual suspend fun isAppUpdateMandatory(): Boolean {
    val context = GlobalContext.get().get<Context>()
    return try {
        val appUpdateInfo = AppUpdateManagerFactory.create(context).requestAppUpdateInfo()
        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            appUpdateInfo.updatePriority() >= MANDATORY_UPDATE_MIN_PRIORITY
    } catch (e: InstallException) {
        false
    }
}

@Composable
actual fun rememberAppUpdateLauncher(): (() -> Unit)? {
    val context = LocalContext.current
    val appUpdateManager = remember(context) { AppUpdateManagerFactory.create(context) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        // A cancelled/failed update just leaves the user where they were - the "Update" button
        // (or the mandatory update screen) stays up so they can try again.
    }

    return remember(appUpdateManager, launcher) {
        {
            appUpdateManager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            launcher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                        )
                    }
                }
                // Same InstallException (e.g. ERROR_APP_NOT_OWNED) as isAppUpdateAvailable() can
                // fail this Task too - same fail-safe response, just leave the user where they are.
                .addOnFailureListener { }
        }
    }
}

@Composable
actual fun MandatoryAppUpdateGate(content: @Composable () -> Unit) {
    var isMandatory by remember { mutableStateOf<Boolean?>(null) }
    val startUpdate = rememberAppUpdateLauncher()

    LaunchedEffect(Unit) {
        isMandatory = isAppUpdateMandatory()
    }

    when (isMandatory) {
        null -> Unit
        true -> {
            LaunchedEffect(startUpdate) { startUpdate?.invoke() }
            MandatoryUpdateScreen(onUpdateClick = { startUpdate?.invoke() })
        }
        false -> content()
    }
}

@Composable
private fun MandatoryUpdateScreen(onUpdateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "A required update is available",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Please update the app to keep using it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Button(onClick = onUpdateClick, modifier = Modifier.padding(top = 16.dp)) {
            Text("Update")
        }
    }
}
