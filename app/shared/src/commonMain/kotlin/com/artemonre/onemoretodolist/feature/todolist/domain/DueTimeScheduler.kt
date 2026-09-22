package com.artemonre.onemoretodolist.feature.todolist.domain

// Android-only concern (like ExactAlarmPermission/SpeechToText) - alarms/notifications don't exist
// as a concept on the other KMP targets this app builds for. Rather than expect/actual (which
// needs zero platform APIs to have a safe default here), this is a plain Koin interface: the
// no-op below is registered as the default in TodoListModule, and Android's own module overrides
// it with the real AlarmManager-backed implementation - the same override pattern
// androidTodoDataModule already uses for TodoPreferences.
interface DueTimeScheduler {
    // Idempotent - cancels any existing alarm for this id, then arms a new one from
    // todo.dueDate/dueTime/dueTimeMode (resolved fresh against the current timezone - see
    // TodoItem.dueInstant), or leaves it cancelled if there's no due time. Safe to call
    // unconditionally on every add/edit/toggle/undo/boot/periodic-maintenance-tick, whether or not
    // the due time actually changed.
    suspend fun reschedule(todo: TodoItem)
    suspend fun cancel(todoId: String)
}

class NoOpDueTimeScheduler : DueTimeScheduler {
    override suspend fun reschedule(todo: TodoItem) = Unit
    override suspend fun cancel(todoId: String) = Unit
}
