package com.artemonre.onemoretodolist.feature.backup.file.data

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.backup.domain.ExportTodos
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupRepository
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupStatus
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getLongOrNullFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

private const val KEY_FILE_URI = "file_backup_uri"
private const val KEY_LAST_BACKUP_AT = "file_backup_last_backup_at"
private const val KEY_NEEDS_NEW_FILE = "file_backup_needs_new_file"

private const val READ_WRITE = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

// Storage Access Framework: the file the user picked (system "create document" UI) comes with a
// persistable permission, which is taken here so the daily worker can keep overwriting it long
// after the picker is gone - no storage permission needed. Combines that file with the local todo
// list (via ExportTodos) and its own bits of state in settings.
@OptIn(ExperimentalSettingsApi::class)
class SafFileAutoBackupRepository(
    private val context: Context,
    private val exportTodos: ExportTodos,
    private val settings: ObservableSettings,
    private val clock: Clock = Clock.System
) : FileAutoBackupRepository {

    override val status: Flow<FileAutoBackupStatus> = combine(
        settings.getStringOrNullFlow(KEY_FILE_URI),
        settings.getLongOrNullFlow(KEY_LAST_BACKUP_AT),
        settings.getBooleanFlow(KEY_NEEDS_NEW_FILE, false)
    ) { fileUri, lastBackupAt, needsNewFile ->
        FileAutoBackupStatus(
            isEnabled = fileUri != null,
            lastBackupAt = lastBackupAt?.let(Instant::fromEpochMilliseconds),
            needsNewFile = needsNewFile
        )
    }

    override val isEnabled: Boolean get() = settings.getStringOrNull(KEY_FILE_URI) != null

    override suspend fun enable(fileUri: String): EmptyResult<DataError.Local> {
        try {
            context.contentResolver.takePersistableUriPermission(fileUri.toUri(), READ_WRITE)
        } catch (e: SecurityException) {
            return Result.Error(DataError.Local.NOT_FOUND)
        }
        releaseCurrentFile()
        settings.putString(KEY_FILE_URI, fileUri)
        settings.remove(KEY_LAST_BACKUP_AT)
        return backup()
    }

    override suspend fun backup(): EmptyResult<DataError.Local> {
        val fileUri = settings.getStringOrNull(KEY_FILE_URI) ?: return Result.Error(DataError.Local.NOT_FOUND)
        val json = exportTodos()
        val result = withContext(Dispatchers.IO) {
            try {
                val stream = context.contentResolver.openOutputStream(fileUri.toUri(), "wt")
                    ?: throw FileNotFoundException("Couldn't open $fileUri")
                stream.bufferedWriter().use { it.write(json) }
                Result.Success(Unit)
            } catch (e: FileNotFoundException) {
                Result.Error(DataError.Local.NOT_FOUND)
            } catch (e: SecurityException) {
                // The persisted permission is gone (e.g. the file's storage provider was removed).
                Result.Error(DataError.Local.NOT_FOUND)
            } catch (e: IOException) {
                Result.Error(DataError.Local.UNKNOWN)
            }
        }
        when (result) {
            is Result.Success -> {
                settings.putLong(KEY_LAST_BACKUP_AT, clock.now().toEpochMilliseconds())
                settings.putBoolean(KEY_NEEDS_NEW_FILE, false)
            }
            is Result.Error -> if (result.error == DataError.Local.NOT_FOUND) settings.putBoolean(KEY_NEEDS_NEW_FILE, true)
        }
        return result
    }

    override fun disable() {
        releaseCurrentFile()
        settings.remove(KEY_FILE_URI)
        settings.remove(KEY_LAST_BACKUP_AT)
        settings.remove(KEY_NEEDS_NEW_FILE)
    }

    // Persisted permissions are capped per app - give back the old file's when switching or stopping.
    private fun releaseCurrentFile() {
        val current = settings.getStringOrNull(KEY_FILE_URI) ?: return
        try {
            context.contentResolver.releasePersistableUriPermission(current.toUri(), READ_WRITE)
        } catch (e: SecurityException) {
            // Already gone - nothing to release.
        }
    }
}
