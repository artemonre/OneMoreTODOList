package com.artemonre.onemoretodolist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.artemonre.onemoretodolist.feature.todolist.domain.HandleDueTodoFired
import com.artemonre.onemoretodolist.feature.todolist.notification.EXTRA_TODO_ID
import com.artemonre.onemoretodolist.feature.todolist.work.WidgetRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// A plain, gateway-owned adapter over the framework-free HandleDueTodoFired - the receiver itself
// lives here (not in app:shared/androidMain) because it must be manifest-declared, and the
// manifest lives in this module, same as MainActivity/QuickAddTodoActivity/the widget receivers.
// Relies on TodoListApplication.onCreate() having already started Koin eagerly on every process
// start (including one triggered fresh by this very broadcast), same precondition
// PeriodicTodoMaintenanceWorker already relies on.
class DueTimeAlarmReceiver : BroadcastReceiver(), KoinComponent {
    private val handleDueTodoFired: HandleDueTodoFired by inject()
    private val postDueNotification: PostDueNotification by inject()
    private val widgetRefresher: WidgetRefresher by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getStringExtra(EXTRA_TODO_ID) ?: return
        // onReceive can't suspend - goAsync() keeps the receiver (and process) alive long enough
        // for the short-lived coroutine below to finish before the system may kill it.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                handleDueTodoFired(todoId)?.let { fired ->
                    postDueNotification(fired)
                    widgetRefresher.refresh()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
