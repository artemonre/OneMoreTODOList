package com.artemonre.onemoretodolist.core.container

import com.artemonre.onemoretodolist.core.domain.AppStartTask
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun `OnStart runs every start task in order, again on each start`() = runTest(testDispatcher) {
        val ran = mutableListOf<String>()
        val viewModel = ContainerViewModel(
            tabs = emptyList(),
            startTasks = listOf(AppStartTask { ran += "first" }, AppStartTask { ran += "second" })
        )

        viewModel.onAction(ContainerAction.OnStart)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onAction(ContainerAction.OnStart)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("first", "second", "first", "second"), ran)
    }

    @Test
    fun `OnTabSelected action updates the selected tab index`() = runTest(testDispatcher) {
        val viewModel = ContainerViewModel(tabs = emptyList(), startTasks = emptyList())

        viewModel.onAction(ContainerAction.OnTabSelected(1))

        assertEquals(1, viewModel.state.value.selectedTabIndex)
    }
}
