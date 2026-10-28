package com.artemonre.onemoretodolist.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.artemonre.onemoretodolist.feature.todolist.domain.HomeScreenWidget

// Every receiver (full, compact, row) shows the same TodoWidget, so one placed instance of any of
// them counts as placed. A request places the full one - the one that lists todos.
class TodoHomeScreenWidget(private val context: Context) : HomeScreenWidget {
    private val glanceAppWidgetManager = GlanceAppWidgetManager(context)

    override suspend fun shouldSuggest(): Boolean =
        glanceAppWidgetManager.getGlanceIds(TodoWidget::class.java).isEmpty()

    // Pinning arrived in API 26, and even there it's up to the launcher to support it.
    override fun canRequestPin(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported

    override suspend fun requestPin(): Boolean =
        glanceAppWidgetManager.requestPinGlanceAppWidget(TodoWidgetReceiver::class.java)
}
