package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val someDate = LocalDate(2026, 1, 1)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `OnReorder action persists renumbered sortOrder in the given order`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                todoItem(id = "1", sortOrder = 0),
                todoItem(id = "2", sortOrder = 1),
                todoItem(id = "3", sortOrder = 2)
            )
        )
        val viewModel = todoListViewModel(dataSource)
        // state (and the private todos it's derived from) uses SharingStarted.WhileSubscribed,
        // so it never starts collecting the data source without an active subscriber - reorder()
        // reads from that same todos flow, so without this the write below is silently skipped.
        backgroundScope.launch { viewModel.state.collect {} }
        // Let the subscription chain above fully warm up the private `todos` flow before
        // dispatching - reorder() reads todos.value synchronously, so if it ran first, it would
        // see the still-empty initial value and silently write nothing.
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(TodoListAction.OnReorder(listOf("3", "1", "2")))
        testDispatcher.scheduler.advanceUntilIdle()

        val sortOrderById = dataSource.observeTodos().first().associate { it.id to it.sortOrder }
        assertEquals(mapOf("3" to 0, "1" to 1, "2" to 2), sortOrderById)
    }

    @Test
    fun `OnSortOptionSelected action updates state sortOption and re-sorts items`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                todoItem(id = "a", text = "Zebra", sortOrder = 0),
                todoItem(id = "b", text = "Apple", sortOrder = 1)
            )
        )
        val viewModel = todoListViewModel(dataSource)
        backgroundScope.launch { viewModel.state.collect {} }

        viewModel.onAction(TodoListAction.OnSortOptionSelected(TodoSortOption.Text))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TodoSortOption.Text, viewModel.state.value.sortOption)
        assertEquals(listOf("Apple", "Zebra"), viewModel.state.value.items.map { it.text })
    }

    private fun todoListViewModel(dataSource: FakeTodoLocalDataSource) =
        TodoListViewModel(dataSource, AddTodo(dataSource), ToggleTodoDone(dataSource))

    private fun todoItem(
        id: String,
        sortOrder: Int,
        text: String = "Todo $id"
    ) = TodoItem(
        id = id,
        text = text,
        status = TodoStatus.Active,
        sortOrder = sortOrder,
        creationDate = someDate,
        lastEditDate = someDate
    )
}
