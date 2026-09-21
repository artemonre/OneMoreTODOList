package com.artemonre.onemoretodolist.feature.todolist.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.dueInstant
import kotlin.time.Duration.Companion.minutes

// Broadcast to whichever component (in the gateway module - app:shared can't reference it
// directly, only the other way around) declares a matching <intent-filter> for this action.
// Package-scoped implicit intent rather than an explicit component reference - see
// DueTimeAlarmReceiver in the gateway module for why.
const val ACTION_TODO_DUE = "com.artemonre.onemoretodolist.ACTION_TODO_DUE"
const val EXTRA_TODO_ID = "todo_id"

// The window is centered close to, but not before, the due time by only 5 minutes - see the
// "Approximate" tab's own copy in TodoFormBottomSheet.kt for the user-facing wording this mirrors.
private val APPROXIMATE_WINDOW_LEAD = 5.minutes
private val APPROXIMATE_WINDOW_LENGTH = 15.minutes

class AndroidAlarmDueTimeScheduler(private val context: Context) : DueTimeScheduler {
    private val alarmManager = context.getSystemService<AlarmManager>()

    override suspend fun reschedule(todo: TodoItem) {
        cancel(todo.id)
        // Resolved fresh against the CURRENT system timezone every time this runs (on save, on
        // toggle, on boot, on a timezone change, ...) - todo.dueDate/dueTime are wall-clock local
        // values, not a fixed instant, so "10am" keeps meaning 10am local even across a timezone
        // change, rather than staying pinned to whatever zone was active when it was first set.
        val dueAt = todo.dueInstant() ?: return
        val pendingIntent = pendingIntentFor(todo.id, create = true) ?: return
        val dueAtMillis = dueAt.toEpochMilliseconds()
        if (todo.dueTimeMode == DueTimeMode.Exact) {
            try {
                alarmManager?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueAtMillis, pendingIntent)
                return
            } catch (e: SecurityException) {
                // Permission revoked after save (the form blocks this at save time, but it can
                // still be revoked afterward in system Settings) - degrade to approximate rather
                // than silently dropping the alarm entirely.
            }
        }
        val windowStart = dueAtMillis - APPROXIMATE_WINDOW_LEAD.inWholeMilliseconds
        alarmManager?.setWindow(AlarmManager.RTC_WAKEUP, windowStart, APPROXIMATE_WINDOW_LENGTH.inWholeMilliseconds, pendingIntent)
    }

    override suspend fun cancel(todoId: String) {
        pendingIntentFor(todoId, create = false)?.let { pendingIntent ->
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun pendingIntentFor(todoId: String, create: Boolean): PendingIntent? {
        val intent = Intent(ACTION_TODO_DUE).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_TODO_ID, todoId)
        }
        val flags = (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE) or PendingIntent.FLAG_IMMUTABLE
        // Stable per-todo request code so the same PendingIntent identity is recovered for
        // cancel/update - hashCode() collision risk is acceptable at this app's scale.
        return PendingIntent.getBroadcast(context, todoId.hashCode(), intent, flags)
    }
}
