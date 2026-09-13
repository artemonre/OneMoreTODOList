package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// Framework-free (no Android/Compose/Glance) so it can run both at app startup (ContainerViewModel)
// and at the start of the widget's own composition (TodoWidget.provideGlance) - the two places a
// recurring todo's due date might need to be caught up without either one being open all the time.
//
// A recurring todo is never a copy: the same id/creationDate gets pulled back to Active and the
// top whenever it's due, regardless of whether it's currently Active or Done - it keeps repeating
// until the todo itself is deleted. Every-type todos fire on a fixed schedule (independent of
// completion); AfterCompletion-type todos only fire once actually completed, interval/unit later.
class ApplyDueRecurrences(
    private val dataSource: TodoLocalDataSource
) {
    suspend operator fun invoke() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        var todos = dataSource.observeTodos().first()
        val dueIds = todos.filter { isDue(it, today) }.map { it.id }

        for (id in dueIds) {
            val item = todos.first { it.id == id }
            val recurrence = item.recurrence ?: continue
            val updated = item.copy(
                status = TodoStatus.Active,
                completionDate = null,
                sortOrder = topSortOrder(todos),
                priorityOrder = prioritize(todos),
                recurrenceAnchorDate = if (recurrence.type == RecurrenceType.Every) today else item.recurrenceAnchorDate
            )
            dataSource.upsertTodo(updated)
            // Keeps sortOrder/priorityOrder stacking correctly if several todos come due in the
            // same pass, instead of every one of them tying for the exact same "top" spot.
            todos = todos.map { if (it.id == id) updated else it }
        }
    }

    private fun isDue(item: TodoItem, today: LocalDate): Boolean {
        val recurrence = item.recurrence ?: return false
        return when (recurrence.type) {
            RecurrenceType.Every -> {
                val anchor = item.recurrenceAnchorDate ?: item.creationDate
                anchor.plus(recurrence) <= today
            }
            RecurrenceType.AfterCompletion -> {
                val completionDate = item.completionDate ?: return false
                completionDate.plus(recurrence) <= today
            }
        }
    }

    private fun prioritize(currentTodos: List<TodoItem>): Double =
        (currentTodos.mapNotNull { it.priorityOrder }.minOrNull() ?: 1.0) * 0.9

    private fun topSortOrder(currentTodos: List<TodoItem>): Int =
        (currentTodos.minOfOrNull { it.sortOrder } ?: 0) - 1
}
