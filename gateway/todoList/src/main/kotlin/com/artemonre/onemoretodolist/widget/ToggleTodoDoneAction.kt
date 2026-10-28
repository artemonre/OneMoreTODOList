package com.artemonre.onemoretodolist.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.artemonre.onemoretodolist.analytics.Analytics
import com.artemonre.onemoretodolist.analytics.AnalyticsEvents
import com.artemonre.onemoretodolist.analytics.AnalyticsSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val todoIdKey: ActionParameters.Key<String> = ActionParameters.Key("todo_id")

class ToggleTodoDoneAction : ActionCallback, KoinComponent {
    private val toggleTodoDone: ToggleTodoDone by inject()
    private val todoLocalDataSource: TodoLocalDataSource by inject()
    private val analytics: Analytics by inject()

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val todoId = parameters[todoIdKey] ?: return
        // A widget tap has no Snackbar/undo surface to fall back on, so it never permanently
        // deletes even when "archive completed todos" is off - it just archives instead.
        //
        // No explicit TodoWidget().update() call needed here - NotifyingTodoLocalDataSource's
        // onDataChanged (wired in TodoListApplication) already calls updateAll() after this write,
        // and collectAsState in TodoWidget.provideGlance re-renders reactively as soon as Room's
        // Flow emits.
        val todoBefore = todoLocalDataSource.observeTodos().first().firstOrNull { it.id == todoId }
        toggleTodoDone(todoId, allowImmediateDelete = false)
        todoBefore?.let {
            analytics.capture(AnalyticsEvents.todoToggled(AnalyticsSource.Widget, archived = true, todoBefore = it))
        }
    }
}
