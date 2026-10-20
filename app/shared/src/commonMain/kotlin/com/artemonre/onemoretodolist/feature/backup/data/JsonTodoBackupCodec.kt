package com.artemonre.onemoretodolist.feature.backup.data

import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.core.sync.BackupJson
import com.artemonre.onemoretodolist.core.sync.CURRENT_BACKUP_FORMAT_VERSION
import com.artemonre.onemoretodolist.core.sync.TodoBackup
import com.artemonre.onemoretodolist.feature.backup.domain.BackupError
import com.artemonre.onemoretodolist.feature.backup.domain.TodoBackupCodec
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

// Just enough of a TodoBackup to check its version before trying to read the rest, which a newer
// format might have changed.
@Serializable
private data class BackupHeader(val formatVersion: Int = CURRENT_BACKUP_FORMAT_VERSION)

// The TodoBackup JSON document from core.sync - shared with the Drive backup and, in its TodoDto
// part, with the future sync server.
class JsonTodoBackupCodec : TodoBackupCodec {
    override fun encode(todos: List<TodoItem>, exportedAt: Instant): String {
        val backup = TodoBackup(
            exportedAt = exportedAt.toEpochMilliseconds(),
            todos = todos.map { it.toTodoDto() }
        )
        return BackupJson.encodeToString(TodoBackup.serializer(), backup)
    }

    override fun decode(document: String): Result<List<TodoItem>, BackupError> {
        return try {
            val header = BackupJson.decodeFromString(BackupHeader.serializer(), document)
            if (header.formatVersion > CURRENT_BACKUP_FORMAT_VERSION) {
                Result.Error(BackupError.NEWER_FORMAT)
            } else {
                val backup = BackupJson.decodeFromString(TodoBackup.serializer(), document)
                Result.Success(backup.todos.mapNotNull { it.toTodoItemOrNull() })
            }
        } catch (e: SerializationException) {
            Result.Error(BackupError.INVALID_FILE)
        } catch (e: IllegalArgumentException) {
            // Not even JSON (decodeFromString reports that as an IllegalArgumentException).
            Result.Error(BackupError.INVALID_FILE)
        }
    }
}
