package com.artemonre.onemoretodolist.feature.backup.file.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupRepository
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupScheduler
import com.artemonre.onemoretodolist.feature.backup.presentation.FileAutoBackupState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L

private data class FileUi(val isBusy: Boolean = false, val failed: Boolean = false)

class FileAutoBackupViewModel(
    private val repository: FileAutoBackupRepository,
    private val scheduler: FileAutoBackupScheduler
) : ViewModel() {

    private val ui = MutableStateFlow(FileUi())

    val state = combine(repository.status, ui) { status, ui ->
        FileAutoBackupState(
            isEnabled = status.isEnabled,
            isBusy = ui.isBusy,
            lastBackupAt = status.lastBackupAt,
            needsNewFile = status.needsNewFile,
            failed = ui.failed
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), FileAutoBackupState())

    // fileUri: the document the user just picked (system file picker).
    fun enable(fileUri: String) {
        viewModelScope.launch {
            ui.update { FileUi(isBusy = true) }
            val failed = when (repository.enable(fileUri)) {
                is Result.Success -> {
                    scheduler.schedule()
                    false
                }
                is Result.Error -> {
                    repository.disable()
                    true
                }
            }
            ui.update { FileUi(failed = failed) }
        }
    }

    fun disable() {
        scheduler.cancel()
        repository.disable()
        ui.update { FileUi() }
    }
}
