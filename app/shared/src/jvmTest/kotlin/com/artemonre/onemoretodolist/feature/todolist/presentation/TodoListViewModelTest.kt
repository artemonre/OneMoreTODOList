package com.artemonre.onemoretodolist.feature.todolist.presentation

import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.EditTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.NoOpDueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.UpdateTopSince
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
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.snackbar_no_text_found

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

    @Test
    fun `OnConfirmEditTodo with isPrioritized moves the item to the top of Manual sort`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource(
            initialTodos = listOf(
                todoItem(id = "1", sortOrder = 0),
                todoItem(id = "2", sortOrder = 1),
                todoItem(id = "3", sortOrder = 2)
            )
        )
        val viewModel = todoListViewModel(dataSource)
        backgroundScope.launch { viewModel.state.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(TodoListAction.OnSortOptionSelected(TodoSortOption.Manual))
        viewModel.onAction(TodoListAction.OnConfirmEditTodo(id = "3", draft = TodoDraft(text = "Todo 3", isPrioritized = true)))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("3", viewModel.state.value.items.first().id)
    }

    @Test
    fun `OnToggleDone moves the item out of Active and into Done`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", sortOrder = 0)))
        val viewModel = todoListViewModel(dataSource)
        backgroundScope.launch { viewModel.state.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(TodoListAction.OnToggleDone("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList(), viewModel.state.value.items.map { it.id })

        viewModel.onAction(TodoListAction.OnFilterSelected(TodoListFilter.Done))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf("1"), viewModel.state.value.items.map { it.id })
    }

    @Test
    fun `OnToggleDone deletes the item immediately when archiving is turned off`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", sortOrder = 0)))
        val todoPreferences = FakeTodoPreferences(initialArchiveCompletedTodos = false)
        val viewModel = todoListViewModel(dataSource, todoPreferences)
        backgroundScope.launch { viewModel.state.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(TodoListAction.OnToggleDone("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList(), dataSource.observeTodos().first())
    }

    @Test
    fun `OnToggleChecklistItem ticks only that item and leaves the todo active`() = runTest(testDispatcher) {
        val checklist = listOf(ChecklistItem(id = "a", text = "First"), ChecklistItem(id = "b", text = "Second"))
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", sortOrder = 0).copy(checklist = checklist)))
        val viewModel = todoListViewModel(dataSource)
        backgroundScope.launch { viewModel.state.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(TodoListAction.OnToggleChecklistItem(todoId = "1", itemId = "b"))
        viewModel.onAction(TodoListAction.OnToggleChecklistItem(todoId = "1", itemId = "a"))
        testDispatcher.scheduler.advanceUntilIdle()

        val persisted = dataSource.observeTodos().first().single()
        assertEquals(listOf(true, true), persisted.checklist.map { it.isDone })
        assertEquals(TodoStatus.Active, persisted.status)
    }

    @Test
    fun `OnConfirmEditTodo saves the draft's checklist and tags and exposes them as known tags`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", sortOrder = 0)))
        val viewModel = todoListViewModel(dataSource)
        backgroundScope.launch { viewModel.state.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()
        val tags = listOf(TodoTag("Work", TagColor.Blue))
        val checklist = listOf(ChecklistItem(id = "a", text = "Step"))

        viewModel.onAction(
            TodoListAction.OnConfirmEditTodo(
                id = "1",
                draft = TodoDraft(text = "Todo 1", isPrioritized = false, checklist = checklist, tags = tags)
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val persisted = dataSource.observeTodos().first().single()
        assertEquals(checklist, persisted.checklist)
        assertEquals(tags, persisted.tags)
        assertEquals(tags, viewModel.state.value.knownTags)
    }

    @Test
    fun `OnConfirmAddScannedTodos adds one todo per line after the existing ones, in order`() = runTest(testDispatcher) {
        val dataSource = FakeTodoLocalDataSource(initialTodos = listOf(todoItem(id = "1", sortOrder = 0)))
        val viewModel = todoListViewModel(dataSource)
        backgroundScope.launch { viewModel.state.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAction(TodoListAction.OnConfirmAddScannedTodos(listOf("Milk", "Bread", "Eggs")))
        testDispatcher.scheduler.advanceUntilIdle()

        val persisted = dataSource.observeTodos().first().sortedBy { it.sortOrder }
        assertEquals(listOf("Todo 1", "Milk", "Bread", "Eggs"), persisted.map { it.text })
        assertEquals(listOf(null, null, null, null), persisted.map { it.priorityOrder })
    }

    @Test
    fun `OnTextRecognized opens the review for lines and shows a snackbar for no text`() = runTest(testDispatcher) {
        val viewModel = todoListViewModel(FakeTodoLocalDataSource(initialTodos = emptyList()))
        val events = mutableListOf<TodoListEvent>()
        backgroundScope.launch { viewModel.events.collect { events += it } }

        viewModel.onAction(TodoListAction.OnTextRecognized(listOf("Milk")))
        viewModel.onAction(TodoListAction.OnTextRecognized(emptyList()))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                TodoListEvent.ShowScannedTextReview(listOf("Milk")),
                TodoListEvent.ShowSnackbar(Res.string.snackbar_no_text_found)
            ),
            events
        )
    }

    private fun todoListViewModel(
        dataSource: FakeTodoLocalDataSource,
        todoPreferences: FakeTodoPreferences = FakeTodoPreferences()
    ) = TodoListViewModel(
        dataSource,
        AddTodo(dataSource, NoOpDueTimeScheduler()),
        EditTodo(dataSource, NoOpDueTimeScheduler()),
        ToggleTodoDone(dataSource, todoPreferences, NoOpDueTimeScheduler()),
        todoPreferences,
        UpdateTopSince(dataSource, todoPreferences),
        NoOpDueTimeScheduler()
    )

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
