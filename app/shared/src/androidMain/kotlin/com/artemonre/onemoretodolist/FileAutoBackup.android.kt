package com.artemonre.onemoretodolist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.feature.backup.file.presentation.FileAutoBackupViewModel
import com.artemonre.onemoretodolist.feature.backup.presentation.FileAutoBackupController
import com.artemonre.onemoretodolist.feature.backup.presentation.FileAutoBackupState
import org.koin.compose.viewmodel.koinViewModel

// One fixed name: the daily backup keeps overwriting this one file.
private const val AUTO_BACKUP_FILE_NAME = "onemoretodolist-auto-backup.json"

@Composable
actual fun rememberFileAutoBackup(): FileAutoBackupController? {
    val viewModel = koinViewModel<FileAutoBackupViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
    // Picking where to save is a UI step - only the chosen document's URI goes to the ViewModel.
    val pickFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) viewModel.enable(uri.toString())
    }
    return remember(viewModel) {
        object : FileAutoBackupController {
            override val state: State<FileAutoBackupState> = state
            override fun setEnabled(enabled: Boolean) {
                if (enabled) pickFile.launch(AUTO_BACKUP_FILE_NAME) else viewModel.disable()
            }
        }
    }
}
