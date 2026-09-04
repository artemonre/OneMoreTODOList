package com.artemonre.onemoretodolist.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.artemonre.onemoretodolist.feature.todolist.domain.ObserveActiveTodos
import com.artemonre.onemoretodolist.feature.todolist.presentation.toTodoItemUi
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val MAX_WIDGET_ROWS = 5

// Android's real widget cell-grid formula (70dp x cells - 30dp) - not this project's Compose-UI
// dp%4 convention, same tradeoff as the provider XML's minWidth/minHeight (see todo_widget_info.xml).
val SIZE_1X1: DpSize = DpSize(40.dp, 40.dp)
val SIZE_2X1: DpSize = DpSize(110.dp, 40.dp)
val SIZE_2X2: DpSize = DpSize(110.dp, 110.dp)

class TodoWidget : GlanceAppWidget(), KoinComponent {
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SIZE_1X1, SIZE_2X1, SIZE_2X2))

    private val observeActiveTodos: ObserveActiveTodos by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val todos by observeActiveTodos()
                .map { todos -> todos.map { it.toTodoItemUi() } }
                .collectAsState(initial = emptyList())

            when (LocalSize.current) {
                SIZE_1X1 -> TodoWidgetContentCompact(activeCount = todos.size)
                SIZE_2X1 -> TodoWidgetContentRow(topTodo = todos.firstOrNull())
                else -> TodoWidgetContent(todos = todos.take(MAX_WIDGET_ROWS))
            }
        }
    }
}
