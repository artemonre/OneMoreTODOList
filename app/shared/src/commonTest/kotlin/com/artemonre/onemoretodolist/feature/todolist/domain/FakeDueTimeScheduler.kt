package com.artemonre.onemoretodolist.feature.todolist.domain

class FakeDueTimeScheduler : DueTimeScheduler {
    val rescheduledIds = mutableListOf<String>()
    val cancelledIds = mutableListOf<String>()

    override suspend fun reschedule(todo: TodoItem) {
        rescheduledIds += todo.id
    }

    override suspend fun cancel(todoId: String) {
        cancelledIds += todoId
    }
}
