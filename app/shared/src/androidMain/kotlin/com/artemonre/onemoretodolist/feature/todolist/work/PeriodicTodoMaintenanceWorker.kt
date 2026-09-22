package com.artemonre.onemoretodolist.feature.todolist.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.artemonre.onemoretodolist.feature.todolist.domain.ApplyDueRecurrences
import com.artemonre.onemoretodolist.feature.todolist.domain.RearmDueTodoAlarms
import java.util.concurrent.TimeUnit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val UNIQUE_WORK_NAME = "periodic_todo_maintenance"

// A gateway-provided hook to repaint the widget - kept as a Koin-injected interface (rather than a
// direct TodoWidget().updateAll() call) since this module can't depend on Glance, and WorkManager
// (not Koin) constructs the Worker itself, so a plain constructor-lambda like androidTodoDataModule's
// onDataChanged isn't an option here.
fun interface WidgetRefresher {
    suspend fun refresh()
}

// Does two unrelated things on the same schedule, since both need is a periodic background wake-up
// regardless of any write happening:
// 1. Catches up recurring todos independently of the app or widget being opened - ContainerViewModel
//    (app open) and TodoWidget.provideGlance (widget composition) both call the same
//    ApplyDueRecurrences use case, but a due todo otherwise only reappears the next time one of
//    those runs.
// 2. Repaints the widget - TopTodoAttention escalates purely from elapsed time, and nothing else
//    pings the widget for that (writes do, via NotifyingTodoLocalDataSource, but time passing alone
//    doesn't write anything).
// 3. Re-arms due-time alarms as a safety net - not the primary delivery path (each due-time todo
//    already gets its own AlarmManager alarm the moment it's saved), just a cheap reconciliation
//    pass in case one was missed (e.g. a boot the BootCompletedReceiver somehow didn't catch).
// WorkManager's own periodic minimum is 15 minutes, so 1h/15m stays close to that floor while
// remaining battery-friendly.
class PeriodicTodoMaintenanceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val applyDueRecurrences: ApplyDueRecurrences by inject()
    private val widgetRefresher: WidgetRefresher by inject()
    private val rearmDueTodoAlarms: RearmDueTodoAlarms by inject()

    override suspend fun doWork(): Result {
        applyDueRecurrences()
        rearmDueTodoAlarms()
        widgetRefresher.refresh()
        return Result.success()
    }
}

// KEEP: if a periodic request with this name is already enqueued (e.g. from a previous app launch),
// leave it running as-is rather than resetting its schedule on every process start.
fun schedulePeriodicTodoMaintenance(context: Context) {
    val request = PeriodicWorkRequestBuilder<PeriodicTodoMaintenanceWorker>(
        repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.HOURS,
        flexTimeInterval = 15, flexTimeIntervalUnit = TimeUnit.MINUTES
    ).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}
