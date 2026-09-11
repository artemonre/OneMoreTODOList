package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

// Fixed id and dates (not computed at seed time), so this is the single source of truth used
// identically by both SeedOnboardingTodos and SeedOnboardingTodosTest - fixed ids are safe since
// seeding only ever runs while the data source is empty. Bump ONBOARDING_TODAY by hand when
// cutting a new release, so onboarding content stays close to release instead of drifting to
// whenever a user happens to install the app.
private val ONBOARDING_TODAY = LocalDate(2026, 9, 1)

// Each item lands one day later than the one before it - onboardingDate(0) is today,
// onboardingDate(1) is tomorrow, and so on - so the list also doubles as a demo of date-based
// sorting.
private fun onboardingDate(daysFromToday: Int): LocalDate =
    ONBOARDING_TODAY.plus(daysFromToday, DateTimeUnit.DAY)

val ONBOARDING_TODOS = listOf(
    TodoItem(
        id = "onboarding-1",
        text = "First todo item",
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = onboardingDate(0),
        lastEditDate = onboardingDate(0)
    ),
    TodoItem(
        id = "onboarding-2",
        text = "Second todo item. By default, items are sorted by date, oldest to newest",
        status = TodoStatus.Active,
        sortOrder = 1,
        creationDate = onboardingDate(1),
        lastEditDate = onboardingDate(1)
    ),
    TodoItem(
        id = "onboarding-3",
        text = "Third todo item. If prioritized (Put to top) it goes to the top of the list",
        status = TodoStatus.Active,
        sortOrder = 2,
        creationDate = onboardingDate(2),
        lastEditDate = onboardingDate(2),
        priorityOrder = 1.0
    ),
    TodoItem(
        id = "onboarding-4",
        text = "Fourth todo item. Swipe left to edit or delete",
        status = TodoStatus.Active,
        sortOrder = 3,
        creationDate = onboardingDate(3),
        lastEditDate = onboardingDate(3)
    ),
    TodoItem(
        id = "onboarding-5",
        text = "Fifth todo item. Swipe right to share",
        status = TodoStatus.Active,
        sortOrder = 4,
        creationDate = onboardingDate(4),
        lastEditDate = onboardingDate(4)
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
