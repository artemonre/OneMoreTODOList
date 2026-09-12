package com.artemonre.onemoretodolist

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.artemonre.onemoretodolist.analytics.initPostHog
import com.artemonre.onemoretodolist.analytics.postHogApiKey
import com.artemonre.onemoretodolist.feature.todolist.di.jvmTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab
import com.artemonre.onemoretodolist.observability.initSentry
import com.artemonre.onemoretodolist.observability.installAwtExceptionReporting
import com.artemonre.onemoretodolist.observability.sentryDsn
import com.posthog.kmp.PostHogContext

// Set by Compose Desktop's own launcher only for a packaged distributable (jpackage output from
// packageDmg/packageMsi/packageDeb) - absent when running via `gradlew run` or the IDE. Desktop has
// no Android-style buildType, so this is the equivalent "is this the real installed app" check.
private fun isPackagedDesktopApp(): Boolean =
    System.getProperty("compose.application.resources.dir") != null

fun main() = application {
    // Skipped outside a packaged install - avoids noise/quota burn from dev-time runs, same
    // reasoning as Android's BuildConfig.DEBUG check.
    if (isPackagedDesktopApp()) {
        initSentry(sentryDsn, environment = "production")
        initPostHog(postHogApiKey, PostHogContext(), platform = "desktop")
    }
    installAwtExceptionReporting()
    Window(
        onCloseRequest = ::exitApplication,
        title = "OneMoreTODOList",
    ) {
        App(
            platformModules = listOf(jvmTodoDataModule()),
            contentTabs = listOf(todoListTab())
        )
    }
}