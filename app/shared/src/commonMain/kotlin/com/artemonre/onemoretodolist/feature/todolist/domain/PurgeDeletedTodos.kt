package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

// How long a deleted todo is kept as a tombstone (see TodoItem.deletedAt). Long enough for a
// deletion to reach every other copy in normal use - a copy left untouched for longer than this
// (an old export, a long-offline device once sync exists) can bring the todo back. Revisit together
// with the sync server.
private val TOMBSTONE_RETENTION = 30.days

class PurgeDeletedTodos(
    private val dataSource: TodoLocalDataSource,
    private val clock: Clock = Clock.System
) {
    suspend operator fun invoke() {
        dataSource.purgeDeletedBefore(clock.now() - TOMBSTONE_RETENTION)
    }
}
