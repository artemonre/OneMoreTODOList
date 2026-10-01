package com.artemonre.onemoretodolist.feature.backup.file.domain

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

data class FileAutoBackupStatus(
    val isEnabled: Boolean,
    val lastBackupAt: Instant?,
    // The chosen file can't be written any more (deleted, moved, its storage removed) - the user
    // has to pick a file again.
    val needsNewFile: Boolean
)

// The daily backup to a file the user picked once. The file is identified by its document URI
// (as a string), which the repository keeps write access to.
interface FileAutoBackupRepository {
    val status: Flow<FileAutoBackupStatus>
    val isEnabled: Boolean

    // Starts backing up to `fileUri` (and writes the first backup right away).
    suspend fun enable(fileUri: String): EmptyResult<DataError.Local>
    suspend fun backup(): EmptyResult<DataError.Local>
    fun disable()
}

interface FileAutoBackupScheduler {
    fun schedule()
    fun cancel()
}
