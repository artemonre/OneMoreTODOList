package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.analytics.ShareMethod
import com.artemonre.onemoretodolist.analytics.TodoForm
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption

sealed interface TodoListAction {
    data class OnToggleDone(val id: String) : TodoListAction
    data class OnSortOptionSelected(val option: TodoSortOption) : TodoListAction
    data class OnFilterSelected(val filter: TodoListFilter) : TodoListAction
    data class OnReorder(val orderedIds: List<String>) : TodoListAction
    data object OnReorderUnavailable : TodoListAction
    data object OnAddTodoClick : TodoListAction
    data object OnAddTodoFullScreenClick : TodoListAction
    // form is which form it came from - QuickSheet or FullForm, for analytics.
    data class OnConfirmAddTodo(val draft: TodoDraft, val form: TodoForm) : TodoListAction
    data class OnEditTodoClick(val id: String) : TodoListAction
    data class OnConfirmEditTodo(val id: String, val draft: TodoDraft) : TodoListAction
    data class OnToggleChecklistItem(val todoId: String, val itemId: String) : TodoListAction
    data class OnDeleteTodo(val id: String) : TodoListAction
    data object OnUndoClick : TodoListAction
    data object OnCapturePhotoClick : TodoListAction
    data class OnTextRecognized(val paragraphs: List<ScannedParagraph>) : TodoListAction
    data class OnConfirmAddScannedTodos(val lines: List<String>) : TodoListAction
    data object OnKeepCompletedTodosClick : TodoListAction
    data object OnAddWidgetClick : TodoListAction
    // Only reported, nothing in the list changes - the screen handles these itself.
    data object OnAddFormExpanded : TodoListAction
    data object OnTodoDetailsOpened : TodoListAction
    data class OnTodoShared(val method: ShareMethod) : TodoListAction
    data object OnEmptyStateTipShown : TodoListAction
    data object OnScannedTextContinuedAsSingle : TodoListAction
    data object OnScannedTextRetaken : TodoListAction
}
