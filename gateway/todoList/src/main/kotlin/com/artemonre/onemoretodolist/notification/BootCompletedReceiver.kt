package com.artemonre.onemoretodolist.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.artemonre.onemoretodolist.feature.todolist.domain.RearmDueTodoAlarms
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// AlarmManager alarms are wiped by the OS on every reboot - this re-arms every todo with a future
// due time once the device (and this app's process) comes back up.
class BootCompletedReceiver : BroadcastReceiver(), KoinComponent {
    private val rearmDueTodoAlarms: RearmDueTodoAlarms by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                rearmDueTodoAlarms()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
