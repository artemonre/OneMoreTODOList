package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.ComposeUiTest
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoSortOption
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.action_delete
import onemoretodolist.app.shared.generated.resources.action_edit
import onemoretodolist.app.shared.generated.resources.action_move_down
import onemoretodolist.app.shared.generated.resources.action_move_to_bottom
import onemoretodolist.app.shared.generated.resources.action_move_to_top
import onemoretodolist.app.shared.generated.resources.action_move_up
import onemoretodolist.app.shared.generated.resources.completed_summary_title
import onemoretodolist.app.shared.generated.resources.share_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

// What TalkBack gets from the todo list: every gesture-only action (swipe, drag) as a card action,
// a labeled checkbox, and no stray buttons from under the cards. Labels are resolved the same way
// the screen resolves them, so the tests don't depend on the machine's locale.
@OptIn(ExperimentalTestApi::class)
class TodoListAccessibilityTest {

    private val todos = listOf("First", "Second", "Third").mapIndexed { index, text ->
        TodoItemUi(
            id = "$index",
            text = text,
            status = TodoStatus.Active,
            sortOrder = index,
            creationDate = LocalDate(2026, 10, 1)
        )
    }

    private fun label(resource: StringResource): String = runBlocking { getString(resource) }

    private val swipeActions get() = listOf(label(Res.string.action_edit), label(Res.string.action_delete), label(Res.string.share_title))

    private fun ComposeUiTest.showList(isReorderEnabled: Boolean, onAction: (TodoListAction) -> Unit = {}) {
        setContent {
            AppTheme(themeConfig = ThemeConfig()) {
                TodoListScreen(
                    state = TodoListState(
                        items = todos,
                        sortOption = TodoSortOption.Manual,
                        isReorderEnabled = isReorderEnabled,
                        activeCount = todos.size
                    ),
                    onAction = onAction
                )
            }
        }
    }

    // The card is the node carrying the actions; its checkbox is a separate one with the same text.
    private fun ComposeUiTest.card(text: String) =
        onNode(hasText(text) and SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))

    private fun ComposeUiTest.actionLabels(text: String): List<String> =
        card(text).fetchSemanticsNode().config[SemanticsActions.CustomActions].map { it.label }

    @Test
    fun `cards offer every swipe action, plus each move that would change the order`() = runComposeUiTest {
        showList(isReorderEnabled = true)
        val toTop = label(Res.string.action_move_to_top)
        val up = label(Res.string.action_move_up)
        val down = label(Res.string.action_move_down)
        val toBottom = label(Res.string.action_move_to_bottom)

        assertEquals(swipeActions + listOf(down, toBottom), actionLabels("First"))
        assertEquals(swipeActions + listOf(toTop, up, down, toBottom), actionLabels("Second"))
        assertEquals(swipeActions + listOf(toTop, up), actionLabels("Third"))
    }

    @Test
    fun `cards offer no moves where dragging isn't allowed`() = runComposeUiTest {
        showList(isReorderEnabled = false)

        assertEquals(swipeActions, actionLabels("Second"))
    }

    @Test
    fun `move actions hand back the whole new order, like a finished drag`() = runComposeUiTest {
        val actions = mutableListOf<TodoListAction>()
        showList(isReorderEnabled = true) { actions += it }

        card("Second").performCustomAccessibilityActionWithLabel(label(Res.string.action_move_up))
        card("First").performCustomAccessibilityActionWithLabel(label(Res.string.action_move_to_bottom))

        assertEquals(
            listOf<TodoListAction>(TodoListAction.OnReorder(listOf("1", "0", "2")), TodoListAction.OnReorder(listOf("1", "2", "0"))),
            actions
        )
    }

    @Test
    fun `swipe actions run from the card`() = runComposeUiTest {
        val actions = mutableListOf<TodoListAction>()
        showList(isReorderEnabled = true) { actions += it }

        card("Second").performCustomAccessibilityActionWithLabel(label(Res.string.action_edit))
        card("Second").performCustomAccessibilityActionWithLabel(label(Res.string.action_delete))

        assertEquals(listOf<TodoListAction>(TodoListAction.OnEditTodoClick("1"), TodoListAction.OnDeleteTodo("1")), actions)
    }

    @Test
    fun `the buttons under a closed card are hidden`() = runComposeUiTest {
        showList(isReorderEnabled = true)

        // The merged tree - what accessibility services see. The unmerged one still holds the
        // icons: clearAndSetSemantics only keeps them out of what's reported.
        onAllNodes(hasContentDescription(label(Res.string.action_edit))).assertCountEquals(0)
        onAllNodes(hasContentDescription(label(Res.string.action_delete))).assertCountEquals(0)
    }

    @Test
    fun `each checkbox is labeled with its todo`() = runComposeUiTest {
        showList(isReorderEnabled = true)

        todos.forEach { onNode(isToggleable() and hasContentDescription(it.text)).assertExists() }
    }

    @Test
    fun `the completed summary title is a heading`() = runComposeUiTest {
        showList(isReorderEnabled = true)

        onNode(hasText(label(Res.string.completed_summary_title)) and isHeading()).assertExists()
    }
}
