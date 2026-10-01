package com.artemonre.onemoretodolist.feature.backup.drive.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.artemonre.onemoretodolist.feature.backup.drive.domain.BackgroundBackupOutcome
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAutoBackupScheduler
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveBackupRepository
import java.util.concurrent.TimeUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val UNIQUE_WORK_NAME = "drive_auto_backup"

// The opt-in daily Google Drive backup. Gets its token silently (DriveAuthorizer) - if Google wants
// the user involved again, it gives up and flags it for Settings instead of retrying forever.
class DriveAutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val repository: DriveBackupRepository by inject()

    override suspend fun doWork(): Result = when (repository.backupWithoutUser()) {
        BackgroundBackupOutcome.Done, BackgroundBackupOutcome.NeedsUser -> Result.success()
        BackgroundBackupOutcome.Retry -> Result.retry()
    }
}

class WorkManagerDriveAutoBackupScheduler(private val context: Context) : DriveAutoBackupScheduler {
    // KEEP: re-enabling (or the startup check below) leaves an already-scheduled run as it is.
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<DriveAutoBackupWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}
