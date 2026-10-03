package com.artemonre.onemoretodolist.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.artemonre.onemoretodolist.MainActivity
import com.artemonre.onemoretodolist.R
import com.artemonre.onemoretodolist.feature.todolist.domain.SnoozeOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.notification.EXTRA_TODO_ID

const val DUE_TODO_CHANNEL_ID = "due_todo"

fun createDueTodoNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(
        DUE_TODO_CHANNEL_ID,
        context.getString(R.string.due_notification_channel_name),
        NotificationManager.IMPORTANCE_HIGH
    )
    context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
}

class PostDueNotification(private val context: Context) {
    operator fun invoke(todo: TodoItem) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            todo.id.hashCode(),
            Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val markDoneIntent = PendingIntent.getBroadcast(
            context,
            todo.id.hashCode(),
            Intent(ACTION_MARK_TODO_DONE).setPackage(context.packageName).putExtra(EXTRA_TODO_ID, todo.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, DUE_TODO_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.due_notification_title))
            .setContentText(todo.text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_action_done, context.getString(R.string.due_notification_action_done), markDoneIntent)
            .addAction(
                R.drawable.ic_action_snooze,
                context.getString(R.string.due_notification_action_snooze_hour),
                snoozeIntent(todo.id, SnoozeOption.OneHour)
            )
            .addAction(
                R.drawable.ic_action_snooze,
                context.getString(R.string.due_notification_action_snooze_tomorrow),
                snoozeIntent(todo.id, SnoozeOption.Tomorrow)
            )
            .build()
        NotificationManagerCompat.from(context).notify(todo.id.hashCode(), notification)
    }

    // Both snooze intents share an action and differ only in extras, which PendingIntent identity
    // ignores - so each option gets its own request code, or the second would overwrite the first.
    private fun snoozeIntent(todoId: String, option: SnoozeOption): PendingIntent = PendingIntent.getBroadcast(
        context,
        (todoId + option.name).hashCode(),
        Intent(ACTION_SNOOZE_TODO)
            .setPackage(context.packageName)
            .putExtra(EXTRA_TODO_ID, todoId)
            .putExtra(EXTRA_SNOOZE_OPTION, option.name),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
