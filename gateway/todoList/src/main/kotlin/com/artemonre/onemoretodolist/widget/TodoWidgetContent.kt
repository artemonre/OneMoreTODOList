package com.artemonre.onemoretodolist.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.artemonre.onemoretodolist.MainActivity
import com.artemonre.onemoretodolist.QuickAddTodoActivity
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoItemUi

@Composable
fun TodoWidgetContent(todos: List<TodoItemUi>) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity<MainActivity>())
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Todos",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "+",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier
                        .clickable(actionStartActivity<QuickAddTodoActivity>())
                        .padding(8.dp)
                )
            }
            Spacer(modifier = GlanceModifier.height(8.dp))
            if (todos.isEmpty()) {
                Text(
                    text = "No active todos",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                )
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                    items(todos, itemId = { it.id.hashCode().toLong() }) { item ->
                        TodoWidgetRow(item)
                    }
                }
            }
        }
    }
}

// 1x1: too small for any todo text - just the active count, tap opens quick-add directly.
@Composable
fun TodoWidgetContentCompact(activeCount: Int) {
    GlanceTheme {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity<QuickAddTodoActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activeCount.toString(),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
            Text(
                text = "todos",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
            )
        }
    }
}

// 2x1: one row tall - the single top active todo, interactive (reuses TodoWidgetRow), plus a
// quick-add affordance since there's no room for a separate header.
@Composable
fun TodoWidgetContentRow(topTodo: TodoItemUi?) {
    GlanceTheme {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (topTodo != null) {
                CheckBox(
                    checked = false,
                    onCheckedChange = actionRunCallback<ToggleTodoDoneAction>(
                        actionParametersOf(todoIdKey to topTodo.id)
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = topTodo.text,
                    maxLines = 1,
                    style = TextStyle(color = GlanceTheme.colors.onSurface),
                    modifier = GlanceModifier.defaultWeight()
                )
            } else {
                Text(
                    text = "No active todos",
                    maxLines = 1,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier.defaultWeight()
                )
            }
            Text(
                text = "+",
                style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier
                    .clickable(actionStartActivity<QuickAddTodoActivity>())
                    .padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun TodoWidgetRow(item: TodoItemUi) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(actionRunCallback<ToggleTodoDoneAction>(actionParametersOf(todoIdKey to item.id)))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBox(
            checked = false,
            onCheckedChange = actionRunCallback<ToggleTodoDoneAction>(actionParametersOf(todoIdKey to item.id))
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = item.text,
            style = TextStyle(color = GlanceTheme.colors.onSurface)
        )
    }
}
