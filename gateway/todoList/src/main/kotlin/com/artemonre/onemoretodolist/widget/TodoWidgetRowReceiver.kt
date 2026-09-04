package com.artemonre.onemoretodolist.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// See TodoWidgetCompactReceiver - same TodoWidget, different default placement size
// (todo_widget_row_info.xml), so it shows as its own entry in the widget picker.
class TodoWidgetRowReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
