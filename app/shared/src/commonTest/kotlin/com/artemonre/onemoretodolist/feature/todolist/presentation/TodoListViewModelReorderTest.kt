package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.EditTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeDueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.UpdateTopSince
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.snackbar_reorder_hint
import onemoretodolist.app.shared.generated.resources.snackbar_switched_to_manual_sort

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelReorderTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `reorder is enabled under Date when its order matches Manual`() = runTest {
        // Created on later days in the same order as their sortOrder - Date and Manual agree.
        val fixture = fixture(listOf(todoItem("a", 0, day = 1), todoItem("b", 1, day = 2)))

        assertTrue(fixture.viewModel.state.value.isReorderEnabled)
    }

    @Test
    fun `reorder is disabled under Date when its order differs from Manual`() = runTest {
        val fixture = fixture(listOf(todoItem("a", 1, day = 1), todoItem("b", 0, day = 2)))

        assertFalse(fixture.viewModel.state.value.isReorderEnabled)
    }

    @Test
    fun `reorder is disabled on the Done filter`() = runTest {
        val fixture = fixture(listOf(todoItem("a", 0, day = 1)), sortOption = TodoSortOption.Manual)

        fixture.viewModel.onAction(TodoListAction.OnFilterSelected(TodoListFilter.Done))

        assertFalse(fixture.viewModel.state.value.isReorderEnabled)
    }

    @Test
    fun `dropping a drag under Date switches to Manual, saves the order and says so`() = runTest {
        val fixture = fixture(listOf(todoItem("a", 0, day = 1), todoItem("b", 1, day = 2)))

        fixture.viewModel.onAction(TodoListAction.OnReorder(listOf("b", "a")))

        assertEquals(TodoSortOption.Manual, fixture.preferences.sortOption.first())
        assertEquals(listOf("b", "a"), fixture.viewModel.state.value.items.map { it.id })
        assertEquals(listOf(TodoListEvent.ShowSnackbar(Res.string.snackbar_switched_to_manual_sort)), fixture.events)
    }

    @Test
    fun `a drag under Date doesn't change the Date order of same-day todos`() = runTest {
        val fixture = fixture(
            listOf(todoItem("a", 0, day = 1, createdAtMillis = 1_000), todoItem("b", 1, day = 1, createdAtMillis = 2_000))
        )

        fixture.viewModel.onAction(TodoListAction.OnReorder(listOf("b", "a")))
        fixture.viewModel.onAction(TodoListAction.OnSortOptionSelected(TodoSortOption.Date))

        assertEquals(listOf("a", "b"), fixture.viewModel.state.value.items.map { it.id })
    }

    @Test
    fun `dropping a drag back in place under Date changes nothing`() = runTest {
        // Non-contiguous sortOrders, as topSortOrder leaves them - still the same order.
        val fixture = fixture(listOf(todoItem("a", -3, day = 1), todoItem("b", 1, day = 2)))

        fixture.viewModel.onAction(TodoListAction.OnReorder(listOf("a", "b")))

        assertEquals(TodoSortOption.Date, fixture.preferences.sortOption.first())
        assertEquals(listOf(-3, 1), fixture.dataSource.observeTodos().first().map { it.sortOrder })
        assertTrue(fixture.events.isEmpty())
    }

    @Test
    fun `reordering under Manual shows no snackbar`() = runTest {
        val fixture = fixture(
            listOf(todoItem("a", 0, day = 1), todoItem("b", 1, day = 2)),
            sortOption = TodoSortOption.Manual
        )

        fixture.viewModel.onAction(TodoListAction.OnReorder(listOf("b", "a")))

        assertEquals(listOf("b", "a"), fixture.viewModel.state.value.items.map { it.id })
        assertTrue(fixture.events.isEmpty())
    }

    @Test
    fun `long-press while reorder is unavailable shows the hint`() = runTest {
        val fixture = fixture(listOf(todoItem("a", 0, day = 1)))

        fixture.viewModel.onAction(TodoListAction.OnReorderUnavailable)

        assertEquals(listOf(TodoListEvent.ShowSnackbar(Res.string.snackbar_reorder_hint)), fixture.events)
    }

    private class Fixture(
        val viewModel: TodoListViewModel,
        val dataSource: FakeTodoLocalDataSource,
        val preferences: FakeTodoPreferences,
        val events: List<TodoListEvent>
    )

    private fun TestScope.fixture(
        todos: List<TodoItem>,
        sortOption: TodoSortOption = TodoSortOption.Date
    ): Fixture {
        val dataSource = FakeTodoLocalDataSource(initialTodos = todos)
        val preferences = FakeTodoPreferences(initialSortOption = sortOption)
        val scheduler = FakeDueTimeScheduler()
        val viewModel = TodoListViewModel(
            todoLocalDataSource = dataSource,
            addTodoUseCase = AddTodo(dataSource, scheduler),
            editTodoUseCase = EditTodo(dataSource, scheduler),
            toggleTodoDoneUseCase = ToggleTodoDone(dataSource, preferences, scheduler),
            todoPreferences = preferences,
            updateTopSince = UpdateTopSince(dataSource, preferences),
            dueTimeScheduler = scheduler
        )
        val events = mutableListOf<TodoListEvent>()
        // state is WhileSubscribed, so it needs a subscriber to stay current.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect { events += it } }
        return Fixture(viewModel, dataSource, preferences, events)
    }

    private fun todoItem(id: String, sortOrder: Int, day: Int, createdAtMillis: Long = 0) = TodoItem(
        id = id,
        text = "Todo $id",
        status = TodoStatus.Active,
        sortOrder = sortOrder,
        creationDate = LocalDate(2026, 1, day),
        lastEditDate = LocalDate(2026, 1, day),
        createdAt = Instant.fromEpochMilliseconds(createdAtMillis)
    )
}
