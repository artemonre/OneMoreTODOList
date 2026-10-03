package com.artemonre.onemoretodolist.feature.backup.domain

import com.artemonre.onemoretodolist.core.domain.Error
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlin.time.Clock
import kotlin.time.Instant

enum class ImportMode {
    // Newest copy of each todo wins; todos only one side has are kept.
    Merge,
    // The file becomes the whole list: anything not in it is deleted.
    Replace
}

enum class BackupError : Error {
    // Not a backup file at all, or a damaged one.
    INVALID_FILE,
    // Written by a newer app version in a format this one can't read.
    NEWER_FORMAT,
    STORAGE
}

// importedCount: how many todos actually changed (added, updated, or - for Replace - deleted).
data class ImportSummary(val importedCount: Int)

// Turns a todo list into a backup document and back - the file format itself (JSON, versioning,
// the wire model) is the data layer's business, see JsonTodoBackupCodec.
interface TodoBackupCodec {
    fun encode(todos: List<TodoItem>, exportedAt: Instant): String

    // Todos that can't be read (e.g. an unknown status from a newer app version) are skipped
    // rather than failing the whole document.
    fun decode(document: String): Result<List<TodoItem>, BackupError>
}

// The live todo list as a backup document - the manual export file and the Drive backup. Deleted
// todos (tombstones) are left out: a backup is a snapshot for a person, not a sync log.
class ExportTodos(
    private val dataSource: TodoLocalDataSource,
    private val codec: TodoBackupCodec,
    private val clock: Clock = Clock.System
) {
    suspend operator fun invoke(): String {
        val todos = dataSource.getAllIncludingDeleted().filter { it.deletedAt == null }
        return codec.encode(todos, exportedAt = clock.now())
    }
}

class ImportTodos(
    private val dataSource: TodoLocalDataSource,
    private val codec: TodoBackupCodec,
    private val dueTimeScheduler: DueTimeScheduler,
    private val clock: Clock = Clock.System
) {
    suspend operator fun invoke(document: String, mode: ImportMode): Result<ImportSummary, BackupError> {
        val incoming = when (val decoded = codec.decode(document)) {
            is Result.Success -> decoded.data
            is Result.Error -> return decoded
        }
        val local = dataSource.getAllIncludingDeleted()
        val toWrite = when (mode) {
            ImportMode.Merge -> mergeTodos(local, incoming)
            ImportMode.Replace -> replaceWith(local, incoming)
        }
        if (toWrite.isEmpty()) return Result.Success(ImportSummary(importedCount = 0))
        if (dataSource.upsertVerbatim(toWrite) is Result.Error) return Result.Error(BackupError.STORAGE)
        toWrite.forEach { todo ->
            if (todo.deletedAt != null) dueTimeScheduler.cancel(todo.id) else dueTimeScheduler.reschedule(todo)
        }
        return Result.Success(ImportSummary(importedCount = toWrite.size))
    }

    // Everything is stamped "now" so the file's contents also win over other copies later (sync,
    // a further merge) - that's what choosing Replace means.
    private fun replaceWith(local: List<TodoItem>, incoming: List<TodoItem>): List<TodoItem> {
        val now = clock.now()
        val incomingIds = incoming.map { it.id }.toSet()
        val localById = local.associateBy { it.id }
        val deletions = local
            .filter { it.deletedAt == null && it.id !in incomingIds }
            .map { it.copy(deletedAt = now, updatedAt = now) }
        val replacements = incoming
            .distinctBy { it.id }
            .map { it.copy(updatedAt = now, deletedAt = null, topSince = localById[it.id]?.topSince) }
        return deletions + replacements
    }
}
