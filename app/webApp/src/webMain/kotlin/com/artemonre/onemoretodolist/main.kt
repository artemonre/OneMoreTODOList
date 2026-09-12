package com.artemonre.onemoretodolist

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.artemonre.onemoretodolist.analytics.initPostHog
import com.artemonre.onemoretodolist.analytics.isLocalhost
import com.artemonre.onemoretodolist.analytics.postHogApiKey
import com.artemonre.onemoretodolist.feature.todolist.di.webTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab
import com.posthog.kmp.PostHogContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Skipped on localhost - avoids noise/quota burn from dev-time runs, same reasoning as
    // Android's BuildConfig.DEBUG check and desktop's isPackagedDesktopApp() check.
    if (!isLocalhost()) {
        initPostHog(postHogApiKey, PostHogContext(), platform = "web")
    }
    ComposeViewport {
        App(
            platformModules = listOf(webTodoDataModule()),
            contentTabs = listOf(todoListTab())
        )
    }
}