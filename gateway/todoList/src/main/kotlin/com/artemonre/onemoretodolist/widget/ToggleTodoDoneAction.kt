package com.artemonre.onemoretodolist.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val todoIdKey: ActionParameters.Key<String> = ActionParameters.Key("todo_id")

class ToggleTodoDoneAction : ActionCallback, KoinComponent {
    private val toggleTodoDone: ToggleTodoDone by inject()

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val todoId = parameters[todoIdKey] ?: return
        // A widget tap has no Snackbar/undo surface to fall back on, so it never permanently
        // deletes even when "archive completed todos" is off - it just archives instead.
        //
        // No explicit TodoWidget().update() call needed here - NotifyingTodoLocalDataSource's
        // onDataChanged (wired in TodoListApplication) already calls updateAll() after this write,
        // and collectAsState in TodoWidget.provideGlance re-renders reactively as soon as Room's
        // Flow emits.
        toggleTodoDone(todoId, allowImmediateDelete = false)
    }
}
