package com.artemonre.onemoretodolist.core.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Bump only for a change older app versions can't read correctly (not for added optional fields,
// which BackupJson already tolerates) - an app refuses a file with a higher version than this
// instead of half-reading it.
const val CURRENT_BACKUP_FORMAT_VERSION = 1

// One whole-list snapshot: the manual export file and the Google Drive backup.
@Serializable
data class TodoBackup(
    val formatVersion: Int = CURRENT_BACKUP_FORMAT_VERSION,
    // Epoch milliseconds.
    val exportedAt: Long,
    val todos: List<TodoDto>
)

// ignoreUnknownKeys: a file written by a newer app version (with fields this one doesn't know yet)
// still reads. Pretty-printed since the export file is something a person may open.
val BackupJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}
