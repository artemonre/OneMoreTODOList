package com.artemonre.onemoretodolist.feature.todolist.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.artemonre.onemoretodolist.feature.todolist.domain.ApplyDueRecurrences
import java.util.concurrent.TimeUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val UNIQUE_WORK_NAME = "apply_due_recurrences"

// Catches up recurring todos on a schedule, independently of the app or widget being opened -
// ContainerViewModel (app open) and TodoWidget.provideGlance (widget composition) both call the same
// ApplyDueRecurrences use case, but a due todo otherwise only reappears the next time one of those
// runs. WorkManager's own periodic minimum is 15 minutes, so 1h/15m stays close to that floor while
// remaining battery-friendly.
class ApplyDueRecurrencesWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val applyDueRecurrences: ApplyDueRecurrences by inject()

    override suspend fun doWork(): Result {
        applyDueRecurrences()
        return Result.success()
    }
}

// KEEP: if a periodic request with this name is already enqueued (e.g. from a previous app launch),
// leave it running as-is rather than resetting its schedule on every process start.
fun schedulePeriodicRecurrenceCheck(context: Context) {
    val request = PeriodicWorkRequestBuilder<ApplyDueRecurrencesWorker>(
        repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = 15, flexTimeIntervalUnit = TimeUnit.MINUTES
    ).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
