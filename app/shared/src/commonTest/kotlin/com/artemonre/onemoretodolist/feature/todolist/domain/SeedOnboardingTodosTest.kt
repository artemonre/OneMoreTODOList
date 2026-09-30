package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class SeedOnboardingTodosTest {

    @Test
    fun `seeds the onboarding todos when the data source is empty`() = runTest {
        val dataSource = FakeTodoLocalDataSource()
        val seedOnboardingTodos = SeedOnboardingTodos(dataSource)

        seedOnboardingTodos()

        assertEquals(ONBOARDING_TODOS, dataSource.observeTodos().first())
    }

    @Test
    fun `seeds localized texts by position, keeping the default for any missing one`() = runTest {
        val dataSource = FakeTodoLocalDataSource()

        SeedOnboardingTodos(dataSource)(listOf("Первая", "Вторая"))

        val seeded = dataSource.observeTodos().first()
        assertEquals(listOf("Первая", "Вторая") + ONBOARDING_TODOS.drop(2).map { it.text }, seeded.map { it.text })
        assertEquals(ONBOARDING_TODOS.map { it.copy(text = "") }, seeded.map { it.copy(text = "") })
    }

    @Test
    fun `does nothing when the data source already has todos`() = runTest {
        val existing = TodoItem(
            id = "existing",
            text = "Already here",
            status = TodoStatus.Active,
            sortOrder = 0,
            creationDate = LocalDate(2026, 1, 1),
            lastEditDate = LocalDate(2026, 1, 1)
        )
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(existing))
        val seedOnboardingTodos = SeedOnboardingTodos(dataSource)

        seedOnboardingTodos()

        assertEquals(listOf(existing), dataSource.observeTodos().first())
    }
}
