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
