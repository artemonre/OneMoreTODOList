package com.artemonre.onemoretodolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.artemonre.onemoretodolist.analytics.Analytics
import com.artemonre.onemoretodolist.analytics.AnalyticsEvents
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab
import org.koin.android.ext.android.inject

// Set by whatever opened the app from outside it - the widget, a reminder notification - so the
// opening can be told apart from a launcher tap. Glance's action parameters arrive as extras too,
// under the parameter key's name.
const val EXTRA_OPENED_FROM = "opened_from"
const val OPENED_FROM_WIDGET = "widget"
const val OPENED_FROM_NOTIFICATION = "notification"

class MainActivity : ComponentActivity() {
    private val analytics: Analytics by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // A recreated activity (rotation, process restart) still carries the intent it was opened
        // with - only the first creation is an opening.
        if (savedInstanceState == null) reportOpenedFrom(intent)

        // TodoListApplication has already started Koin (with androidTodoDataModule included)
        // by the time any Activity runs - platformModules stays empty here rather than
        // registering that module a second time.
        setContent {
            App(
                platformModules = emptyList(),
                contentTabs = listOf(todoListTab())
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        reportOpenedFrom(intent)
    }

    private fun reportOpenedFrom(intent: Intent) {
        when (intent.getStringExtra(EXTRA_OPENED_FROM)) {
            OPENED_FROM_WIDGET -> analytics.capture(AnalyticsEvents.widgetOpenedApp())
            OPENED_FROM_NOTIFICATION -> analytics.capture(AnalyticsEvents.notificationOpened())
        }
    }
}
