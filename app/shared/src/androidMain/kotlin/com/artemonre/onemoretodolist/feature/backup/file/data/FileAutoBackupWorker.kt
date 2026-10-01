package com.artemonre.onemoretodolist.feature.backup.file.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.Result as DomainResult
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupRepository
import com.artemonre.onemoretodolist.feature.backup.file.domain.FileAutoBackupScheduler
import java.util.concurrent.TimeUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val UNIQUE_WORK_NAME = "file_auto_backup"

// The opt-in daily backup to a file the user picked. A file that's gone is flagged for Settings
// (the repository does that) rather than retried - only the user can pick a new one.
class FileAutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val repository: FileAutoBackupRepository by inject()

    override suspend fun doWork(): Result {
        if (!repository.isEnabled) return Result.success()
        return when (val backup = repository.backup()) {
            is DomainResult.Success -> Result.success()
            is DomainResult.Error ->
                if (backup.error == DataError.Local.NOT_FOUND) Result.success() else Result.retry()
        }
    }
}

class WorkManagerFileAutoBackupScheduler(private val context: Context) : FileAutoBackupScheduler {
    // KEEP: re-enabling (or the startup check) leaves an already-scheduled run as it is.
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<FileAutoBackupWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
