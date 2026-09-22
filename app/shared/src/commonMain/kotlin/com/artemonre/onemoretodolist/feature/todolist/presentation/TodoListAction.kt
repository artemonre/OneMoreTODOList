package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeMode
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

sealed interface TodoListAction {
    data class OnToggleDone(val id: String) : TodoListAction
    data class OnSortOptionSelected(val option: TodoSortOption) : TodoListAction
    data class OnReorder(val orderedIds: List<String>) : TodoListAction
    data object OnAddTodoClick : TodoListAction
    data object OnAddTodoFullScreenClick : TodoListAction
    data class OnConfirmAddTodo(
        val text: String,
        val isPrioritized: Boolean,
        val recurrence: Recurrence? = null,
        val dueDate: LocalDate? = null,
        val dueTime: LocalTime? = null,
        val dueTimeMode: DueTimeMode? = null
    ) : TodoListAction
    data class OnEditTodoClick(val id: String) : TodoListAction
    data class OnConfirmEditTodo(
        val id: String,
        val text: String,
        val isPrioritized: Boolean,
        val recurrence: Recurrence? = null,
        val dueDate: LocalDate? = null,
        val dueTime: LocalTime? = null,
        val dueTimeMode: DueTimeMode? = null
    ) : TodoListAction
    data class OnDeleteTodo(val id: String) : TodoListAction
    data object OnUndoClick : TodoListAction
}
