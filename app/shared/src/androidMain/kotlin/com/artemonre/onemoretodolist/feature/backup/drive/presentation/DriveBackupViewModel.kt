package com.artemonre.onemoretodolist.feature.backup.drive.presentation

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode
import com.artemonre.onemoretodolist.feature.backup.domain.ImportTodos
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAuthorization
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAuthorizer
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAutoBackupScheduler
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveBackupRepository
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupMessage
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L

// What to carry on with once the user has been through Google's consent screen.
private enum class DriveOperation { Backup, Restore, EnableAutoBackup }

private data class DriveUi(
    val isBusy: Boolean = false,
    val isChoosingRestoreMode: Boolean = false,
    val message: DriveBackupMessage? = null
)

class DriveBackupViewModel(
    private val repository: DriveBackupRepository,
    private val authorizer: DriveAuthorizer,
    private val importTodos: ImportTodos,
    private val scheduler: DriveAutoBackupScheduler
) : ViewModel() {

    private val ui = MutableStateFlow(DriveUi())
    private var pendingOperation: DriveOperation? = null
    private var pendingRestoreJson: String? = null

    // Google's consent screen needs an Activity to launch it - the composable does that.
    private val _consentRequests = Channel<IntentSender>()
    val consentRequests = _consentRequests.receiveAsFlow()

    val state = combine(repository.status, ui) { status, ui ->
        DriveBackupState(
            isConnected = status.isConnected,
            isBusy = ui.isBusy,
            lastBackupAt = status.lastBackupAt,
            isAutoBackupEnabled = status.isAutoBackupEnabled,
            needsReconnect = status.needsReconnect,
            isChoosingRestoreMode = ui.isChoosingRestoreMode,
            message = ui.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), DriveBackupState())

    fun backupNow() = runWithAccess(DriveOperation.Backup)

    fun restore() = runWithAccess(DriveOperation.Restore)

    fun setAutoBackup(enabled: Boolean) {
        if (enabled) {
            // Ask for access now, while the user is here - the daily worker can't show a consent screen.
            runWithAccess(DriveOperation.EnableAutoBackup)
        } else {
            repository.setAutoBackupEnabled(false)
            scheduler.cancel()
        }
    }

    fun confirmRestore(mode: ImportMode) {
        val json = pendingRestoreJson ?: return
        pendingRestoreJson = null
        viewModelScope.launch {
            ui.update { it.copy(isChoosingRestoreMode = false, isBusy = true) }
            val message = when (val result = importTodos(json, mode)) {
                is Result.Success -> DriveBackupMessage.Restored(result.data.importedCount)
                is Result.Error -> DriveBackupMessage.RestoreFailed(result.error)
            }
            ui.update { it.copy(isBusy = false, message = message) }
        }
    }

    fun cancelRestore() {
        pendingRestoreJson = null
        ui.update { it.copy(isChoosingRestoreMode = false) }
    }

    fun disconnect() {
        viewModelScope.launch {
            ui.update { it.copy(isBusy = true, message = null) }
            scheduler.cancel()
            val token = (authorizer.authorize() as? DriveAuthorization.Granted)?.accessToken
            repository.disconnect(token)
            ui.update { it.copy(isBusy = false) }
        }
    }

    fun onConsentResult(data: Intent?) {
        val operation = pendingOperation ?: return
        pendingOperation = null
        viewModelScope.launch {
            val token = authorizer.tokenFromConsentResult(data)
            if (token == null) {
                ui.update { it.copy(isBusy = false, message = DriveBackupMessage.AccessDenied) }
            } else {
                perform(operation, token)
            }
        }
    }

    private fun runWithAccess(operation: DriveOperation) {
        viewModelScope.launch {
            ui.update { it.copy(isBusy = true, message = null) }
            when (val authorization = authorizer.authorize()) {
                is DriveAuthorization.Granted -> perform(operation, authorization.accessToken)
                is DriveAuthorization.NeedsConsent -> {
                    pendingOperation = operation
                    _consentRequests.send(authorization.consentScreen)
                }
                DriveAuthorization.NotConfigured -> ui.update { it.copy(isBusy = false, message = DriveBackupMessage.NotConfigured) }
                DriveAuthorization.Failed -> ui.update { it.copy(isBusy = false, message = DriveBackupMessage.Failed) }
            }
        }
    }

    private suspend fun perform(operation: DriveOperation, token: String) {
        ui.update { it.copy(isBusy = true) }
        when (operation) {
            DriveOperation.Backup -> finishBackup(token)
            DriveOperation.EnableAutoBackup -> {
                repository.setAutoBackupEnabled(true)
                scheduler.schedule()
                // Back up right away too, so there's something in Drive from day one.
                finishBackup(token)
            }
            DriveOperation.Restore -> when (val result = repository.download(token)) {
                is Result.Success -> {
                    val json = result.data
                    if (json == null) {
                        ui.update { it.copy(isBusy = false, message = DriveBackupMessage.NoBackupFound) }
                    } else {
                        pendingRestoreJson = json
                        ui.update { it.copy(isBusy = false, isChoosingRestoreMode = true) }
                    }
                }
                is Result.Error -> ui.update { it.copy(isBusy = false, message = result.error.toMessage()) }
            }
        }
    }

    private suspend fun finishBackup(token: String) {
        val message = when (val result = repository.backup(token)) {
            is Result.Success -> DriveBackupMessage.BackedUp
            is Result.Error -> result.error.toMessage()
        }
        ui.update { it.copy(isBusy = false, message = message) }
    }
}

private fun DataError.Remote.toMessage(): DriveBackupMessage = when (this) {
    DataError.Remote.NO_INTERNET -> DriveBackupMessage.NoInternet
    DataError.Remote.UNAUTHORIZED -> DriveBackupMessage.AccessDenied
    DataError.Remote.SERVER_ERROR, DataError.Remote.SERIALIZATION, DataError.Remote.UNKNOWN -> DriveBackupMessage.Failed
}
