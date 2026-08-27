package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class SeedOnboardingTodosTest {

    @Test
    fun `seeds three onboarding todos when the data source is empty`() = runTest {
        val dataSource = FakeTodoLocalDataSource()
        val seedOnboardingTodos = SeedOnboardingTodos(dataSource)

        seedOnboardingTodos()

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todos = dataSource.observeTodos().first()

        assertEquals(3, todos.size)
        assertTodoExists(todos, text = "First todo item", date = today, priorityOrder = null)
        assertTodoExists(
            todos,
            text = "Sorted by date items put from latest to newest",
            date = today.plus(1, DateTimeUnit.DAY),
            priorityOrder = null
        )
        assertTodoExists(
            todos,
            text = "If prioritized (Put to top) it goes to the top of the list",
            date = today,
            priorityOrder = 1.0
        )
    }

    @Test
    fun `does nothing when the data source already has todos`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val existing = TodoItem(
            id = "existing",
            text = "Already here",
            status = TodoStatus.Active,
            sortOrder = 0,
            creationDate = today,
            lastEditDate = today
        )
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(existing))
        val seedOnboardingTodos = SeedOnboardingTodos(dataSource)

        seedOnboardingTodos()

        assertEquals(listOf(existing), dataSource.observeTodos().first())
    }

    private fun assertTodoExists(
        todos: List<TodoItem>,
        text: String,
        date: LocalDate,
        priorityOrder: Double?
    ) {
        assertTrue(
            todos.any { it.text == text && it.creationDate == date && it.priorityOrder == priorityOrder },
            "Expected a todo with text \"$text\" dated $date with priorityOrder $priorityOrder, got $todos"
        )
    }
}
