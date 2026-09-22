package com.artemonre.onemoretodolist.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.artemonre.onemoretodolist.R
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem

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
        val notification = NotificationCompat.Builder(context, DUE_TODO_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("The time to do it has come")
            .setContentText(todo.text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(context).notify(todo.id.hashCode(), notification)
    }
}
