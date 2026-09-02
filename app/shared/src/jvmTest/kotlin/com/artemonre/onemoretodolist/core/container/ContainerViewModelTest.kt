package com.artemonre.onemoretodolist.core.container

import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.ONBOARDING_TODOS
import com.artemonre.onemoretodolist.feature.todolist.domain.SeedOnboardingTodos
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ContainerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnStart action triggers onboarding seeding`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource()
        val viewModel = ContainerViewModel(contentTabs = emptyList(), seedOnboardingTodos = SeedOnboardingTodos(dataSource))

        viewModel.onAction(ContainerAction.OnStart)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ONBOARDING_TODOS.size, dataSource.observeTodos().first().size)
    }

    @Test
    fun `OnTabSelected action updates the selected tab index`() = runTest(testDispatcher) {
        val viewModel = ContainerViewModel(
            contentTabs = emptyList(),
            seedOnboardingTodos = SeedOnboardingTodos(FakeTodoLocalDataSource())
        )

        viewModel.onAction(ContainerAction.OnTabSelected(1))

        assertEquals(1, viewModel.state.value.selectedTabIndex)
    }
}
