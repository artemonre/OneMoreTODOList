package com.artemonre.onemoretodolist.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import androidx.appfunctions.AppFunctionStringValueConstraint
import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.EditTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.knownTags
import com.artemonre.onemoretodolist.feature.todolist.domain.sortedByOption
import com.artemonre.onemoretodolist.feature.todolist.work.WidgetRefresher
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// On-device agent entry point (Android 16+): exposes the todo list to authorized agents, such as
// the system assistant, as AppFunctions - the platform's on-device equivalent of MCP tools - with
// the same capabilities as the app itself: read, add, edit (text, priority, reminder, recurrence,
// checklist, tags), complete/reopen, and delete. The appfunctions compiler generates the concrete
// TodoAppFunctionService (declared in the manifest) plus the XML the OS indexes. Every function
// goes through the same domain use cases as the app, so agent changes follow the same rules (top
// ordering, alarms, recurrence); dependencies come from Koin, which TodoListApplication starts on
// every process start, this one included.
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "TodoAppFunctionService",
    appFunctionXmlFileName = "todo_app_function_service",
)
abstract class BaseTodoAppFunctionService : AppFunctionService(), KoinComponent {
    private val todoLocalDataSource: TodoLocalDataSource by inject()
    private val todoPreferences: TodoPreferences by inject()
    private val addTodo: AddTodo by inject()
    private val editTodo: EditTodo by inject()
    private val toggleTodoDone: ToggleTodoDone by inject()
    private val dueTimeScheduler: DueTimeScheduler by inject()
    private val widgetRefresher: WidgetRefresher by inject()

    /**
     * Gets the user's todos with all their details. Active todos come in the same order the user
     * sees them in the app; completed ones come most recently completed first.
     * Call this first to find a todo's ID before calling "editTodo", "changeTodoDoneStatus" or "deleteTodo".
     *
     * @param status Which todos to return: "ACTIVE" for ones still to do, "DONE" for completed ones, or "ALL" for both (active first).
     * @return The matching todos, possibly empty.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getTodoList(
        @AppFunctionStringValueConstraint(enumValues = ["ACTIVE", "DONE", "ALL"])
        status: String,
    ): List<TodoSummary> {
        val todos = currentTodos()
        val active = todos.filter { it.status == TodoStatus.Active }.sortedByOption(todoPreferences.sortOption.first())
        val done = todos.filter { it.status == TodoStatus.Done }.sortedByDescending { it.completionDate }
        val selected = when (status) {
            "ACTIVE" -> active
            "DONE" -> done
            "ALL" -> active + done
            else -> throw AppFunctionInvalidArgumentException("Invalid status: $status. Must be ACTIVE, DONE, or ALL.")
        }
        return selected.map { it.toTodoSummary() }
    }

    /**
     * Adds a new todo to the user's list.
     *
     * @param text What needs to be done. Cannot be empty or blank.
     * @param putToTop Whether to pin the todo to the top of the list, for something urgent or important.
     * @param reminderDateTime Optional local date and time in ISO-8601 (e.g. "2026-10-03T09:30") at which to notify the user. Must be in the future. For a repeating todo, this is the first occurrence and sets the time of day for the following ones.
     * @param exactReminder Whether the reminder must fire at the exact time rather than within a 15-minute window around it. May fall back to the window if the user hasn't allowed exact alarms.
     * @param recurrence Optional repeat rule. Without a reminder, the todo simply becomes active again when the interval has passed.
     * @param checklist Optional checklist items; pass null as each item's ID.
     * @param tags Optional tags. Reusing an existing tag name keeps that tag's color.
     * @return The created todo.
     * @throws AppFunctionInvalidArgumentException If text is blank, reminderDateTime isn't a valid future ISO-8601 local date-time, or the recurrence, or a tag color, is invalid. If thrown, ask the user to clarify.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun addTodo(
        text: String,
        putToTop: Boolean = false,
        reminderDateTime: String? = null,
        exactReminder: Boolean = false,
        recurrence: TodoRecurrence? = null,
        checklist: List<TodoChecklistItem>? = null,
        tags: List<TodoTagItem>? = null,
    ): TodoSummary = agentInput {
        if (text.isBlank()) {
            throw AppFunctionInvalidArgumentException("Todo text cannot be empty")
        }
        val reminder = reminderDateTime?.let { parseFutureReminder(it) }
        val created = addTodo(
            text = text.trim(),
            isPrioritized = putToTop,
            recurrence = recurrence?.toRecurrence(),
            dueDate = reminder?.date,
            dueTime = reminder?.time,
            dueTimeMode = reminder?.let { dueTimeMode(exactReminder) },
            checklist = checklist?.toChecklist().orEmpty(),
            tags = tags?.toTags(currentTodos().knownTags()).orEmpty()
        )
        widgetRefresher.refresh()
        created.toTodoSummary()
    }

    /**
     * Changes an existing todo. Only the parameters you pass are changed - leave a parameter null
     * to keep its current value.
     * Required workflow: Call "getTodoList" first to obtain the todo's ID and current details.
     *
     * @param todoId The ID of the todo, obtained from getTodoList.
     * @param text New text for the todo.
     * @param putToTop Whether the todo should be pinned to the top of the list.
     * @param reminderDateTime New reminder as a local date and time in ISO-8601 (e.g. "2026-10-03T09:30"), must be in the future. For a repeating todo this moves the whole series to the new time of day. Only active todos can have a reminder.
     * @param exactReminder Whether the reminder must fire at the exact time rather than within a 15-minute window around it.
     * @param clearReminder Pass true to remove the reminder. Takes priority over reminderDateTime.
     * @param recurrence New repeat rule, replacing the current one.
     * @param clearRecurrence Pass true to stop the todo from repeating. Takes priority over recurrence.
     * @param checklist The complete new checklist, replacing the current one. To change one item, send back the whole list from getTodoList with that item edited, keeping the other items' IDs. Pass an empty list to remove the checklist.
     * @param tags The complete new set of tags, replacing the current ones. Pass an empty list to remove all tags.
     * @return The todo after the change.
     * @throws AppFunctionElementNotFoundException If no todo has this ID. If thrown, call "getTodoList" to find the correct ID.
     * @throws AppFunctionInvalidArgumentException If reminderDateTime isn't a valid future ISO-8601 local date-time, a reminder is set on a completed todo, or the recurrence, or a tag color, is invalid. If thrown, ask the user to clarify.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun editTodo(
        todoId: String,
        text: String? = null,
        putToTop: Boolean? = null,
        reminderDateTime: String? = null,
        exactReminder: Boolean? = null,
        clearReminder: Boolean? = null,
        recurrence: TodoRecurrence? = null,
        clearRecurrence: Boolean? = null,
        checklist: List<TodoChecklistItem>? = null,
        tags: List<TodoTagItem>? = null,
    ): TodoSummary = agentInput {
        val todos = currentTodos()
        val item = todos.firstOrNull { it.id == todoId }
            ?: throw AppFunctionElementNotFoundException("No todo found for ID: $todoId")
        val values = item.mergeAgentEdit(
            text = text,
            putToTop = putToTop,
            reminder = if (clearReminder == true) null else reminderDateTime?.let { parseFutureReminder(it) },
            exactReminder = exactReminder,
            clearReminder = clearReminder == true,
            recurrence = if (clearRecurrence == true) null else recurrence?.toRecurrence(),
            clearRecurrence = clearRecurrence == true,
            checklist = checklist?.toChecklist(),
            tags = tags?.toTags(todos.knownTags())
        )
        val updated = editTodo(
            id = todoId,
            text = values.text,
            isPrioritized = values.isPrioritized,
            recurrence = values.recurrence,
            dueDate = values.dueDate,
            dueTime = values.dueTime,
            dueTimeMode = values.dueTimeMode,
            checklist = values.checklist,
            tags = values.tags
        ) ?: throw AppFunctionElementNotFoundException("No todo found for ID: $todoId")
        widgetRefresher.refresh()
        updated.toTodoSummary()
    }

    /**
     * Changes whether a todo is done: marks it done, or reopens a completed one. Asking for the
     * state it's already in changes nothing. A repeating todo marked done becomes active again on
     * its own when its next occurrence is due - tell the user so.
     * Required workflow: Call "getTodoList" first to obtain the todo's ID.
     *
     * @param todoId The ID of the todo, obtained from getTodoList.
     * @param isDone True to mark the todo done, false to reopen it.
     * @return The todo after the change.
     * @throws AppFunctionElementNotFoundException If no todo has this ID. If thrown, call "getTodoList" to find the correct ID.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun changeTodoDoneStatus(todoId: String, isDone: Boolean): TodoSummary {
        val item = findTodo(todoId)
        if ((item.status == TodoStatus.Done) != isDone) {
            // No undo surface here, same as the widget and notification - so never the
            // archive-off "delete immediately" branch, see ToggleTodoDone.
            toggleTodoDone(todoId, allowImmediateDelete = false)
            widgetRefresher.refresh()
        }
        return findTodo(todoId).toTodoSummary()
    }

    /**
     * Permanently deletes a todo, including its checklist and reminder. This can't be undone -
     * always confirm with the user before calling it. To just finish a todo, use "changeTodoDoneStatus" instead.
     * Required workflow: Call "getTodoList" first to obtain the todo's ID.
     *
     * @param todoId The ID of the todo, obtained from getTodoList.
     * @return The todo as it was right before being deleted.
     * @throws AppFunctionElementNotFoundException If no todo has this ID. If thrown, call "getTodoList" to find the correct ID.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun deleteTodo(todoId: String): TodoSummary {
        val item = findTodo(todoId)
        todoLocalDataSource.deleteTodo(todoId)
        dueTimeScheduler.cancel(todoId)
        widgetRefresher.refresh()
        return item.toTodoSummary()
    }

    private inline fun <T> agentInput(block: () -> T): T = try {
        block()
    } catch (e: InvalidAgentInputException) {
        throw AppFunctionInvalidArgumentException(e.message)
    }

    private suspend fun currentTodos(): List<TodoItem> = todoLocalDataSource.observeTodos().first()

    private suspend fun findTodo(todoId: String): TodoItem =
        currentTodos().firstOrNull { it.id == todoId }
            ?: throw AppFunctionElementNotFoundException("No todo found for ID: $todoId")
}
