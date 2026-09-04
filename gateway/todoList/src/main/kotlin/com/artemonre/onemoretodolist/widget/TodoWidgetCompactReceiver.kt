package com.artemonre.onemoretodolist.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// A second, separately pickable widget-picker entry for the exact same TodoWidget - it just
// defaults to a smaller placement size (see todo_widget_compact_info.xml's targetCellWidth/
// Height) so the user can pick their preferred size directly from the picker, instead of only
// being able to shrink it by hand after adding the (still fully resizable) default one.
class TodoWidgetCompactReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}
