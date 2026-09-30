package com.artemonre.onemoretodolist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.artemonre.onemoretodolist.feature.todolist.domain.SnoozeOption
import com.artemonre.onemoretodolist.feature.todolist.domain.SnoozeTodo
import com.artemonre.onemoretodolist.feature.todolist.notification.EXTRA_TODO_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

const val ACTION_SNOOZE_TODO = "com.artemonre.onemoretodolist.ACTION_SNOOZE_TODO"
const val EXTRA_SNOOZE_OPTION = "snooze_option"

// Backs the notification's snooze action buttons - same shape as MarkTodoDoneReceiver. The todo
// itself doesn't change position or content, so there's no widget refresh here; the snooze alarm
// firing later goes through DueTimeAlarmReceiver like any other due alarm.
class SnoozeTodoReceiver : BroadcastReceiver(), KoinComponent {
    private val snoozeTodo: SnoozeTodo by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val todoId = intent.getStringExtra(EXTRA_TODO_ID) ?: return
        val option = SnoozeOption.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_SNOOZE_OPTION) } ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                snoozeTodo(todoId, option)
                NotificationManagerCompat.from(context).cancel(todoId.hashCode())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
