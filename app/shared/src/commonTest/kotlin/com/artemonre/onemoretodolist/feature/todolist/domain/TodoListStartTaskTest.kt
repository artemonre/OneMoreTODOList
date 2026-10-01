package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class TodoListStartTaskTest {

    @Test
    fun `seeds the onboarding todos in the given language into an empty list`() = runTest {
        val dataSource = FakeTodoLocalDataSource()
        val texts = ONBOARDING_TODOS.indices.map { "Sample $it" }
        val task = TodoListStartTask(
            SeedOnboardingTodos(dataSource),
            ApplyDueRecurrences(dataSource),
            PurgeDeletedTodos(dataSource),
            OnboardingTexts { texts }
        )

        task.run()

        assertEquals(texts.toSet(), dataSource.observeTodos().first().map { it.text }.toSet())
    }
}
