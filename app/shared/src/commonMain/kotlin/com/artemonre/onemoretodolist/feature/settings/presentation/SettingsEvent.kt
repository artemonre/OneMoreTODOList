package com.artemonre.onemoretodolist.feature.settings.presentation

// Things only the UI can do - opening the platform's save/open file dialogs.
sealed interface SettingsEvent {
    data class SaveBackupFile(val suggestedName: String, val json: String) : SettingsEvent
    data object PickBackupFile : SettingsEvent
}
