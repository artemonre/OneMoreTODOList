package com.artemonre.onemoretodolist

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.artemonre.onemoretodolist.analytics.initPostHog
import com.artemonre.onemoretodolist.core.theme.di.androidThemeModule
import com.artemonre.onemoretodolist.feature.todolist.di.androidTodoDataModule
import com.artemonre.onemoretodolist.observability.initSentry
import com.artemonre.onemoretodolist.widget.TodoWidget
import com.posthog.kmp.PostHogContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// Koin is otherwise only started lazily, the first time MainActivity composes App() - but a
// GlanceAppWidget/GlanceAppWidgetReceiver can run in a fresh process where that has never
// happened (e.g. a scheduled widget update, or the quick-add Activity, after the OS kills the
// app and later restarts just that entry point). Starting Koin here in Application.onCreate()
// guarantees it always exists before any component runs, Activity or widget alike.
class TodoListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Skipped in debug builds - avoids noise/quota burn from dev-time crashes and test data.
        if (!BuildConfig.DEBUG) {
            initSentry(BuildConfig.SENTRY_DSN, environment = "production")
            initPostHog(
                BuildConfig.POSTHOG_API_KEY,
                PostHogContext(this@TodoListApplication),
                platform = "android"
            )
        }
        startKoin {
            androidContext(this@TodoListApplication)
            modules(
                appKoinModules(
                    listOf(
                        androidTodoDataModule(this@TodoListApplication) {
                            // The widget's own actions (its checkbox) already get redrawn by
                            // Glance automatically - this covers every other write (the app, the
                            // quick-add popup), which the widget's passive Flow collection alone
                            // isn't guaranteed to pick up promptly.
                            TodoWidget().updateAll(this@TodoListApplication)
                        },
                        androidThemeModule {
                            // Same rationale as above: a Settings change (e.g. color palette)
                            // needs an explicit refresh to reach the widget promptly.
                            TodoWidget().updateAll(this@TodoListApplication)
                        }
                    )
                )
            )
        }
    }
}
