package com.artemonre.onemoretodolist

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.artemonre.onemoretodolist.analytics.initPostHog
import com.artemonre.onemoretodolist.core.ads.AdConfig
import com.artemonre.onemoretodolist.core.ads.di.androidAdsModule
import com.artemonre.onemoretodolist.core.theme.di.androidThemeModule
import com.artemonre.onemoretodolist.feature.todolist.di.androidDueTimeModule
import com.artemonre.onemoretodolist.feature.todolist.di.androidTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.work.WidgetRefresher
import com.artemonre.onemoretodolist.feature.todolist.work.schedulePeriodicTodoMaintenance
import com.artemonre.onemoretodolist.notification.PostDueNotification
import com.artemonre.onemoretodolist.notification.createDueTodoNotificationChannel
import com.artemonre.onemoretodolist.observability.initSentry
import com.artemonre.onemoretodolist.widget.TodoWidget
import com.google.android.gms.ads.MobileAds
import com.posthog.kmp.PostHogContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

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
        createDueTodoNotificationChannel(this@TodoListApplication)
        MobileAds.initialize(this@TodoListApplication)
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
                        },
                        androidDueTimeModule(this@TodoListApplication),
                        androidAdsModule(
                            this@TodoListApplication,
                            AdConfig(
                                bannerAdUnitId = BuildConfig.ADMOB_BANNER_AD_UNIT_ID,
                                interstitialAdUnitId = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID
                            )
                        ),
                        module {
                            // Lets PeriodicTodoMaintenanceWorker repaint the widget without
                            // app:shared depending on Glance - WorkManager (not Koin) constructs
                            // the worker itself, so this can't just be a constructor lambda like
                            // onDataChanged above.
                            single<WidgetRefresher> { WidgetRefresher { TodoWidget().updateAll(this@TodoListApplication) } }
                            // Lives here rather than androidDueTimeModule - it needs R.drawable,
                            // which only this module's own R class exposes.
                            single { PostDueNotification(this@TodoListApplication) }
                        }
                    )
                )
            )
        }
        schedulePeriodicTodoMaintenance(this@TodoListApplication)

        // The hooks above only refresh the widget lazily, after a todo/theme write - so a process
        // restart with no write yet (e.g. right after an app update) would otherwise leave every
        // widget instance showing whatever it last rendered until the next write, the 30-minute
        // updatePeriodMillis fallback, or the hourly PeriodicTodoMaintenanceWorker tick. This forces
        // a fresh provideGlance() pass on every cold start instead.
        CoroutineScope(Dispatchers.Default).launch {
            TodoWidget().updateAll(this@TodoListApplication)
        }
    }
}
