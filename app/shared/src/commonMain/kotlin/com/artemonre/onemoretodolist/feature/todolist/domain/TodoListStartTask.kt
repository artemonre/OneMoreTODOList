package com.artemonre.onemoretodolist.feature.todolist.domain

import com.artemonre.onemoretodolist.core.domain.AppStartTask

// The first-launch sample todos' text in the current language - only read while the list is
// still empty. Resolved outside the domain (string resources), see ResourceOnboardingTexts.
fun interface OnboardingTexts {
    suspend fun texts(): List<String>
}

// Runs on every app start/foreground, not just a cold start - a recurring todo can come due, and
// tombstones can expire, while the app sits in the background.
class TodoListStartTask(
    private val seedOnboardingTodos: SeedOnboardingTodos,
    private val applyDueRecurrences: ApplyDueRecurrences,
    private val purgeDeletedTodos: PurgeDeletedTodos,
    private val onboardingTexts: OnboardingTexts
) : AppStartTask {
    override suspend fun run() {
        seedOnboardingTodos(onboardingTexts.texts())
        applyDueRecurrences()
        purgeDeletedTodos()
    }
}
