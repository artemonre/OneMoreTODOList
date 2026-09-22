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
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.notification.EXTRA_TODO_ID

const val DUE_TODO_CHANNEL_ID = "due_todo"

fun createDueTodoNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(DUE_TODO_CHANNEL_ID, "Todo due times", NotificationManager.IMPORTANCE_HIGH)
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
            .setContentTitle("The time to do it has come")
            .setContentText(todo.text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_action_done, "Done", markDoneIntent)
            .build()
        NotificationManagerCompat.from(context).notify(todo.id.hashCode(), notification)
    }
}
