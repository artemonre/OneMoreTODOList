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
import androidx.glance.color.ColorProviders
import com.artemonre.onemoretodolist.core.designsystem.theme.toColorPalette
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
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
    private val themeRepository: ThemeRepository by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val todos by observeActiveTodos()
                .map { todos -> todos.map { it.toTodoItemUi() } }
                .collectAsState(initial = emptyList())
            val themeConfig by themeRepository.themeConfig.collectAsState(initial = ThemeConfig())
            val palette = themeConfig.palette.toColorPalette()
            // surfaceContainer isn't one of the roles ColorProviders exposes, so it's carried
            // separately from the rest of the palette, which flows through GlanceTheme normally.
            val colors: ColorProviders = androidx.glance.material3.ColorProviders(light = palette.light, dark = palette.dark)
            val background = androidx.glance.color.ColorProvider(
                day = palette.light.surfaceContainer,
                night = palette.dark.surfaceContainer
            )

            when (LocalSize.current) {
                SIZE_1X1 -> TodoWidgetContentCompact(activeCount = todos.size, colors = colors, background = background)
                SIZE_2X1 -> TodoWidgetContentRow(topTodo = todos.firstOrNull(), colors = colors, background = background)
                else -> TodoWidgetContent(todos = todos.take(MAX_WIDGET_ROWS), colors = colors, background = background)
            }
        }
    }
}
