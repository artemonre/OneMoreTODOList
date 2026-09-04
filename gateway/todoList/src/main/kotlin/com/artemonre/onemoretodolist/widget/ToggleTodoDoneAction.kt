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
        toggleTodoDone(todoId)
        // Belt-and-suspenders: collectAsState in TodoWidget.provideGlance already re-renders
        // reactively as soon as Room's Flow emits, but explicitly requesting an update here
        // guarantees an immediate refresh regardless of the Glance session's own lifecycle.
        TodoWidget().update(context, glanceId)
    }
}
