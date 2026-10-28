package com.artemonre.onemoretodolist.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.artemonre.onemoretodolist.analytics.Analytics
import com.artemonre.onemoretodolist.analytics.AnalyticsEvents
import com.artemonre.onemoretodolist.analytics.WidgetSize
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TodoWidgetReceiver : WidgetPlacementReportingReceiver(WidgetSize.Full) {
    override val glanceAppWidget: GlanceAppWidget = TodoWidget()
}

// onEnabled/onDisabled fire for the first instance of this receiver's widget placed and the last
// one removed - so widget_placed/widget_removed count home screens with the widget, not instances.
abstract class WidgetPlacementReportingReceiver(private val size: WidgetSize) : GlanceAppWidgetReceiver(), KoinComponent {
    private val analytics: Analytics by inject()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        analytics.capture(AnalyticsEvents.widgetPlaced(size))
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        analytics.capture(AnalyticsEvents.widgetRemoved(size))
    }
}
