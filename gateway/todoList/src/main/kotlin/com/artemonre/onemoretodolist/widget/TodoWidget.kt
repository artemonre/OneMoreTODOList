package com.artemonre.onemoretodolist.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProviders
import com.artemonre.onemoretodolist.core.designsystem.theme.toColorPalette
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.artemonre.onemoretodolist.feature.todolist.domain.ObserveActiveTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.presentation.toTodoItemUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// These classify the widget's current *actual* size (from SizeMode.Exact below) into
// small/row/full layout by what actually fits, not by Android's theoretical minWidth/minHeight
// grid-cell formula (70dp x cells - 30dp, i.e. "one cell" = 40dp) that todo_widget_info.xml's
// minWidth/minHeight are declared with - most launchers hand back a real size well above that
// formula's floor even for a nominally "1 cell" widget, so comparing against 40dp classified
// everything as either cramped into a layout with no room for it, or as compact when there was
// actually room for a real row. MIN_WIDTH_FOR_TEXT is a checkbox + a few words + padding; a
// narrower widget can't show a todo's text at all, only an icon/count.
private val MIN_WIDTH_FOR_TEXT = 100.dp

// TodoWidgetContent's own layout, added up: 16dp (Column's 8dp top/bottom padding) + 48dp (the
// header row's height is set by its "+" Box, which is a fixed 48dp square) + 8dp (the Spacer
// below the header) + ~40dp per todo row (TodoWidgetRow's 4dp top/bottom padding plus its
// checkbox/text content) - so header + 2 rows needs on the order of 150dp. Anything shorter than
// that has room for a header and, at best, one cramped/partial row - not a real list, just the
// single-row layout's job.
private val MIN_HEIGHT_FOR_LIST = 160.dp

// The full-list layout's LazyColumn is a genuinely scrollable list (Glance backs it with a real
// RemoteViewsService-based adapter, not a static render) - so this is just a sane ceiling on how
// much data to ever load, not "however many rows fit the visible height". Capping to the visible
// height instead would hand the scrollable list only exactly enough items to fill the screen,
// leaving nothing for a scroll gesture to actually reveal.
private const val MAX_WIDGET_ROWS = 30

class TodoWidget : GlanceAppWidget(), KoinComponent {
    // Exact, not a fixed Responsive size set - the widget providers allow unbounded resize
    // (resizeMode="horizontal|vertical", no maxResizeWidth/Height declared), so the row count and
    // clickable area both need to track the widget's *actual* current size. A fixed Responsive set
    // caps every widget at whatever its largest declared size is: content (and thus the click
    // target) gets rendered for that small canvas regardless of how much bigger the widget is
    // actually resized to, leaving the extra space dead - both the "resizing doesn't show more
    // todos" and "the bottom part isn't clickable" bugs traced back to this.
    override val sizeMode: SizeMode = SizeMode.Exact

    private val observeActiveTodos: ObserveActiveTodos by inject()
    private val themeRepository: ThemeRepository by inject()
    private val todoPreferences: TodoPreferences by inject()

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Collected reactively via flatMapLatest (not read once with .first()) so a sort change
        // is picked up even while a Glance session is still alive: updateAll() only triggers a
        // recomposition of an already-running session rather than a fresh provideGlance call, so
        // a one-shot snapshot here would silently miss the change until the session had happened
        // to already tear down (~45s idle) - which is why the old code updated inconsistently.
        //
        // Archived isn't an ordering of the active list at all (it's the Done-only view) - same
        // fallback UpdateTopSince uses, so the widget's order and its notion of "top" always agree.
        val activeTodos = todoPreferences.sortOption
            .map { it.takeUnless { option -> option == TodoSortOption.Archived } ?: TodoSortOption.Date }
            .flatMapLatest { sortOption -> observeActiveTodos(sortOption) }
            .map { todos -> todos.map { it.toTodoItemUi() } }
        val themeConfig = themeRepository.themeConfig

        // Fetched before provideContent (instead of defaulting collectAsState to emptyList()/
        // ThemeConfig()) so a fresh Glance session's very first frame already shows real data - a
        // session tears down after ~45s idle, and every new one otherwise flashes empty content
        // until the first Flow emission lands, which is very visible since AppWidget rendering is
        // a RemoteViews swap, not an animated Compose recomposition.
        //
        // Recurring-todo catch-up deliberately no longer runs here - it's off this render path,
        // relying instead on app open (ContainerScreen's OnStart) and the hourly
        // ApplyDueRecurrencesWorker to keep todos due even when only the widget is ever looked at.
        val initialTodos = activeTodos.first()
        val initialThemeConfig = themeConfig.first()

        provideContent {
            val todos by activeTodos.collectAsState(initial = initialTodos)
            val theme by themeConfig.collectAsState(initial = initialThemeConfig)
            val palette = theme.palette.toColorPalette()
            // surfaceContainer isn't one of the roles ColorProviders exposes, so it's carried
            // separately from the rest of the palette, which flows through GlanceTheme normally.
            val colors: ColorProviders = androidx.glance.material3.ColorProviders(light = palette.light, dark = palette.dark)
            val background = androidx.glance.color.ColorProvider(
                day = palette.light.surfaceContainer,
                night = palette.dark.surfaceContainer
            )

            val size = LocalSize.current
            when {
                size.width < MIN_WIDTH_FOR_TEXT ->
                    TodoWidgetContentCompact(activeCount = todos.size, colors = colors, background = background)
                size.height < MIN_HEIGHT_FOR_LIST ->
                    TodoWidgetContentRow(topTodo = todos.firstOrNull(), colors = colors, background = background)
                else ->
                    TodoWidgetContent(todos = todos.take(MAX_WIDGET_ROWS), colors = colors, background = background)
            }
        }
    }
}
