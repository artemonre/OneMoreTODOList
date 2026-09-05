package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

// Fixed ids and dates (not computed at seed time), so this is the single source of truth used
// identically by both SeedOnboardingTodos and SeedOnboardingTodosTest - fixed ids are safe since
// seeding only ever runs while the data source is empty. Bump ONBOARDING_TODAY/ONBOARDING_TOMORROW
// by hand when cutting a new release, so onboarding content stays close to release instead of
// drifting to whenever a user happens to install the app.
private val ONBOARDING_TODAY = LocalDate(2026, 9, 1)
private val ONBOARDING_TOMORROW = LocalDate(2026, 9, 2)

val ONBOARDING_TODOS = listOf(
    TodoItem(
        id = "onboarding-1",
        text = "First todo item",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = ONBOARDING_TODAY,
        lastEditDate = ONBOARDING_TODAY
    ),
    TodoItem(
        id = "onboarding-2",
        text = "Second todo item. By default, items are sorted by date, oldest to newest",
        status = TodoStatus.Active,
        sortOrder = 1,
        creationDate = ONBOARDING_TOMORROW,
        lastEditDate = ONBOARDING_TOMORROW
    ),
    TodoItem(
        id = "onboarding-3",
        text = "Third todo item. If prioritized (Put to top) it goes to the top of the list",
        status = TodoStatus.Active,
        sortOrder = 2,
        creationDate = ONBOARDING_TODAY,
        lastEditDate = ONBOARDING_TODAY,
        priorityOrder = 1.0
    ),
    TodoItem(
        id = "onboarding-4",
        text = "Fourth todo item. Click it to see the full text if it doesn't fit, and copy it from there",
        status = TodoStatus.Active,
        sortOrder = 3,
        creationDate = ONBOARDING_TODAY,
        lastEditDate = ONBOARDING_TODAY
    ),
    TodoItem(
        id = "onboarding-5",
        text = "Fifth todo item. Swipe left to edit or delete",
        status = TodoStatus.Active,
        sortOrder = 4,
        creationDate = ONBOARDING_TODAY,
        lastEditDate = ONBOARDING_TODAY
    ),
    TodoItem(
        id = "onboarding-6",
        text = "Sixth todo item. Swipe right to share",
        status = TodoStatus.Active,
        sortOrder = 5,
        creationDate = ONBOARDING_TODAY,
        lastEditDate = ONBOARDING_TODAY
    )
)

// Runs once at app startup (see App.kt) rather than from a ViewModel's init block,
// so it stays a plain, independently testable unit instead of an untestable side effect.
class SeedOnboardingTodos(
    private val todoLocalDataSource: TodoLocalDataSource
) {
    suspend operator fun invoke() {
        if (todoLocalDataSource.observeTodos().first().isNotEmpty()) return

        ONBOARDING_TODOS.forEach { todoLocalDataSource.upsertTodo(it) }
    }
}
