package com.artemonre.onemoretodolist.core.container

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.navigation3.runtime.NavKey
import com.artemonre.onemoretodolist.analytics.RecordingAnalytics
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
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.tab_settings
import onemoretodolist.app.shared.generated.resources.tab_todo
import org.jetbrains.compose.resources.StringResource

private data object TestRoute : NavKey

private fun tab(label: StringResource) = NavigationTab(
    label = label,
    icon = Icons.Filled.Settings,
    startDestination = TestRoute,
    entries = {},
    registerRoutes = {}
)

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

    @Test
    fun `switching tabs reports the new tab by its label key, staying on one reports nothing`() = runTest(testDispatcher) {
        val analytics = RecordingAnalytics()
        val viewModel = ContainerViewModel(
            tabs = listOf(tab(Res.string.tab_todo), tab(Res.string.tab_settings)),
            startTasks = emptyList(),
            analytics = analytics
        )

        viewModel.onAction(ContainerAction.OnTabSelected(1))
        viewModel.onAction(ContainerAction.OnTabSelected(1))

        assertEquals(listOf("settings"), analytics.events.map { it.properties["tab"] })
    }
}
