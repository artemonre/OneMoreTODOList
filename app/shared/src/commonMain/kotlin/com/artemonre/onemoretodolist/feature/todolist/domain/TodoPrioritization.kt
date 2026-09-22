package com.artemonre.onemoretodolist.feature.todolist.domain

// Shared "put to top" math - used by AddTodo/TodoListViewModel (the "Put to top" checkbox),
// ApplyDueRecurrences (a recurring todo coming back due), and HandleDueTodoFired (a due-time
// notification firing), so all four "move this to the top" paths stay in exact agreement.

fun topSortOrder(currentTodos: List<TodoItem>): Int =
    (currentTodos.minOfOrNull { it.sortOrder } ?: 0) - 1

fun topPriorityOrder(currentTodos: List<TodoItem>): Double =
    (currentTodos.mapNotNull { it.priorityOrder }.minOrNull() ?: 1.0) * 0.9
