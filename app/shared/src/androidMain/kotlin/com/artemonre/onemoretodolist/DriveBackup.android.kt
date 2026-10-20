package com.artemonre.onemoretodolist

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.presentation.ObserveAsEvents
import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode
import com.artemonre.onemoretodolist.feature.backup.drive.presentation.DriveBackupViewModel
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupController
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupState
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun rememberDriveBackup(): DriveBackupController? {
    val viewModel = koinViewModel<DriveBackupViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onConsentResult(if (result.resultCode == Activity.RESULT_OK) result.data else null)
    }
    ObserveAsEvents(viewModel.consentRequests) { intentSender ->
        consentLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
    }
    return remember(viewModel) {
        object : DriveBackupController {
            override val state: State<DriveBackupState> = state
            override fun backupNow() = viewModel.backupNow()
            override fun restore() = viewModel.restore()
            override fun confirmRestore(mode: ImportMode) = viewModel.confirmRestore(mode)
            override fun cancelRestore() = viewModel.cancelRestore()
            override fun setAutoBackup(enabled: Boolean) = viewModel.setAutoBackup(enabled)
            override fun disconnect() = viewModel.disconnect()
        }
    }
}
