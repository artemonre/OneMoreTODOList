package com.artemonre.onemoretodolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoFormBody
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// Launched from the widget's "+" button. The manifest theme makes the window full-screen and
// transparent so whatever's behind (home screen, the app) stays fully visible; only the card
// itself is drawn, tapping outside it dismisses. No ViewModel - TodoFormBody already owns its
// own text/checkbox state, and this screen has exactly one action to perform.
class QuickAddTodoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val addTodo = koinInject<AddTodo>()
            val coroutineScope = rememberCoroutineScope()

            AppTheme(paintBackground = false) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { finish() }
                        .imePadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(fraction = 0.9f),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.background,
                        shadowElevation = 6.dp
                    ) {
                        TodoFormBody(
                            editingItem = null,
                            onConfirm = { text, isPrioritized ->
                                coroutineScope.launch { addTodo(text, isPrioritized) }
                                finish()
                            },
                            onDismiss = { finish() },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}
