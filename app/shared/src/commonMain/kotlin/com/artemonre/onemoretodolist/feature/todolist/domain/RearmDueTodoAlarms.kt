package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlinx.coroutines.flow.first

// Shared by the boot receiver (alarms are wiped on reboot) and the periodic maintenance worker
// (a cheap safety net, not the primary delivery path). Both just need "make sure every still-due
// todo has an alarm armed for the current timezone" - due times are wall-clock local, not fixed
// instants (see TodoItem.dueInstant), so this also naturally re-resolves every alarm against
// whatever timezone is current whenever it runs. Deliberately NOT triggered directly off a
// timezone-change broadcast - a receiver's execution window is too short to safely reschedule an
// unbounded number of alarms without risking some being dropped if the process is killed
// mid-loop; the hourly worker (and next boot) catching up is an accepted, bounded delay instead.
// Re-schedules unconditionally rather than first checking whether one is already armed:
// reschedule() is a cheap cancel+set, and AlarmManager has no query API for that anyway.
class RearmDueTodoAlarms(
    private val dataSource: TodoLocalDataSource,
    private val dueTimeScheduler: DueTimeScheduler
) {
    suspend operator fun invoke() {
        val now = Clock.System.now()
        dataSource.observeTodos().first()
            .filter { it.status == TodoStatus.Active }
            .filter { (it.dueInstant() ?: return@filter false) > now }
            .forEach { dueTimeScheduler.reschedule(it) }
    }
}
