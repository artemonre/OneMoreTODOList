package com.artemonre.onemoretodolist.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.CheckboxDefaults
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProviders
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.artemonre.onemoretodolist.MainActivity
import com.artemonre.onemoretodolist.QuickAddTodoActivity
import com.artemonre.onemoretodolist.feature.todolist.domain.TopTodoAttention
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoItemUi

@Composable
fun TodoWidgetContent(todos: List<TodoItemUi>, colors: ColorProviders, background: ColorProvider) {
    GlanceTheme(colors = colors) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(background)
                .clickable(actionStartActivity<MainActivity>())
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Todos",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .clickable(actionStartActivity<QuickAddTodoActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                }
            }
            Spacer(modifier = GlanceModifier.height(8.dp))
            if (todos.isEmpty()) {
                Text(
                    text = "No active todos",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant)
                )
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                    items(todos, itemId = { it.id.hashCode().toLong() }) { item ->
                        TodoWidgetRow(item)
                    }
                }
            }
        }
    }
}

// 1x1: too small for any todo text - the active count (tap opens the app) stacked over a "+"
// zone that fills all the remaining height (tap opens quick-add).
@Composable
fun TodoWidgetContentCompact(activeCount: Int, colors: ColorProviders, background: ColorProvider) {
    GlanceTheme(colors = colors) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(background)
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatWidgetCount(activeCount),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
            }
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .clickable(actionStartActivity<QuickAddTodoActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                )
            }
        }
    }
}

private fun formatWidgetCount(count: Int): String = if (count > 99) "99+" else count.toString()

// null means "no override" (default row background/text). Only ever non-null for the top todo -
// see TopSinceTrackingTodoLocalDataSource/topTodoAttention.
@Composable
private fun TopTodoAttention.background(): ColorProvider? = when (this) {
    TopTodoAttention.None -> null
    TopTodoAttention.Primary -> GlanceTheme.colors.primary
    TopTodoAttention.PrimaryContainer -> GlanceTheme.colors.primaryContainer
    TopTodoAttention.Error -> GlanceTheme.colors.error
}

@Composable
private fun TopTodoAttention.content(): ColorProvider = when (this) {
    TopTodoAttention.None -> GlanceTheme.colors.onSurface
    TopTodoAttention.Primary -> GlanceTheme.colors.onPrimary
    TopTodoAttention.PrimaryContainer -> GlanceTheme.colors.onPrimaryContainer
    TopTodoAttention.Error -> GlanceTheme.colors.onError
}

// 2x1: one row tall - the single top active todo, interactive (reuses TodoWidgetRow), plus a
// quick-add affordance since there's no room for a separate header.
@Composable
fun TodoWidgetContentRow(topTodo: TodoItemUi?, colors: ColorProviders, background: ColorProvider) {
    GlanceTheme(colors = colors) {
        val attentionBackground = topTodo?.attention?.background()
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(attentionBackground ?: background)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (topTodo != null) {
                CheckBox(
                    checked = false,
                    onCheckedChange = actionRunCallback<ToggleTodoDoneAction>(
                        actionParametersOf(todoIdKey to topTodo.id)
                    ),
                    colors = CheckboxDefaults.checkBoxColors(
                        checkedColor = GlanceTheme.colors.primary,
                        uncheckedColor = GlanceTheme.colors.primary
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
            }
            // Fills all the row's height and remaining width - not just the checkbox/plus
            // zones - so it reads as one big tap target that opens the app.
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = topTodo?.text ?: "No active todos",
                    maxLines = 3,
                    style = TextStyle(
                        color = when {
                            topTodo == null -> GlanceTheme.colors.onSurfaceVariant
                            attentionBackground != null -> topTodo.attention.content()
                            else -> GlanceTheme.colors.onSurface
                        }
                    ),
                    modifier = GlanceModifier.padding(vertical = 12.dp)
                )
            }
            // No width modifier - Glance only has an exact width(), not a true minimum, and an
            // exact value narrower than the glyph actually renders at (larger font scale/density)
            // clips it instead of growing. Wrapping to its own content is the only way to
            // guarantee it's never cut off; the 8dp padding just grows the tap target a bit
            // beyond the glyph itself.
            Box(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .clickable(actionStartActivity<QuickAddTodoActivity>())
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(color = GlanceTheme.colors.primary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                )
            }
        }
    }
}

@Composable
internal fun TodoWidgetRow(item: TodoItemUi) {
    val attentionBackground = item.attention.background()
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .let { if (attentionBackground != null) it.background(attentionBackground) else it }
            .padding(vertical = 4.dp, horizontal = if (attentionBackground != null) 4.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckBox(
            checked = false,
            onCheckedChange = actionRunCallback<ToggleTodoDoneAction>(actionParametersOf(todoIdKey to item.id)),
            colors = CheckboxDefaults.checkBoxColors(
                checkedColor = GlanceTheme.colors.primary,
                uncheckedColor = GlanceTheme.colors.primary
            )
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        // Fills the row's remaining width so tapping the text (not just the checkbox) opens the
        // app instead of toggling the todo done - same pattern as TodoWidgetContentRow.
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = item.text,
                maxLines = 3,
                style = TextStyle(color = if (attentionBackground != null) item.attention.content() else GlanceTheme.colors.onSurface)
            )
        }
    }
}
