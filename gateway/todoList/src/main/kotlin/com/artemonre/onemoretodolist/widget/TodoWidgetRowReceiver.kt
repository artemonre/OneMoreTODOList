package com.artemonre.onemoretodolist.widget

import androidx.glance.appwidget.GlanceAppWidget
import com.artemonre.onemoretodolist.analytics.WidgetSize

// See TodoWidgetCompactReceiver - same TodoWidget, different default placement size
// (todo_widget_row_info.xml), so it shows as its own entry in the widget picker.
class TodoWidgetRowReceiver : WidgetPlacementReportingReceiver(WidgetSize.Row) {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
