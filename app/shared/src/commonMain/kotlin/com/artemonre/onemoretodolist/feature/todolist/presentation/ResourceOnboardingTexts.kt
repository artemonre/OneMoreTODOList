package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.OnboardingTexts
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.onboarding_todo_1
import onemoretodolist.app.shared.generated.resources.onboarding_todo_2
import onemoretodolist.app.shared.generated.resources.onboarding_todo_3
import onemoretodolist.app.shared.generated.resources.onboarding_todo_4
import onemoretodolist.app.shared.generated.resources.onboarding_todo_5
import org.jetbrains.compose.resources.getString

// Order matches ONBOARDING_TODOS - SeedOnboardingTodos replaces each sample's text by position.
class ResourceOnboardingTexts : OnboardingTexts {
    override suspend fun texts(): List<String> = listOf(
        getString(Res.string.onboarding_todo_1),
        getString(Res.string.onboarding_todo_2),
        getString(Res.string.onboarding_todo_3),
        getString(Res.string.onboarding_todo_4),
        getString(Res.string.onboarding_todo_5)
    )
}
