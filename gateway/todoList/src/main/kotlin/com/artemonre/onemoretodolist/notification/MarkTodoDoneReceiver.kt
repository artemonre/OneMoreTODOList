package com.artemonre.onemoretodolist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.artemonre.onemoretodolist.analytics.Analytics
import com.artemonre.onemoretodolist.analytics.AnalyticsEvents
import com.artemonre.onemoretodolist.analytics.AnalyticsSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.notification.EXTRA_TODO_ID
import com.artemonre.onemoretodolist.feature.todolist.work.WidgetRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val ACTION_MARK_TODO_DONE = "com.artemonre.onemoretodolist.ACTION_MARK_TODO_DONE"

// Backs the notification's "Done" action button - fired by a PendingIntent, same shape as
// DueTimeAlarmReceiver. Same "no Snackbar/undo surface available" reasoning as the widget's
// checkbox (see ToggleTodoDoneAction), so this never immediately deletes either.
class MarkTodoDoneReceiver : BroadcastReceiver(), KoinComponent {
    private val toggleTodoDone: ToggleTodoDone by inject()
    private val todoLocalDataSource: TodoLocalDataSource by inject()
    private val analytics: Analytics by inject()
    private val widgetRefresher: WidgetRefresher by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getStringExtra(EXTRA_TODO_ID) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val todoBefore = todoLocalDataSource.observeTodos().first().firstOrNull { it.id == todoId }
                toggleTodoDone(todoId, allowImmediateDelete = false)
                todoBefore?.let {
                    analytics.capture(
                        AnalyticsEvents.todoToggled(AnalyticsSource.Notification, archived = true, todoBefore = it)
                    )
                }
                NotificationManagerCompat.from(context).cancel(todoId.hashCode())
                widgetRefresher.refresh()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
