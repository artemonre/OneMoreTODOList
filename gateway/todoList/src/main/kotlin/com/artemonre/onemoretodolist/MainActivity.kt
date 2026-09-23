package com.artemonre.onemoretodolist

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.artemonre.onemoretodolist.debug.DebugMenuGate
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab

class MainActivity : ComponentActivity() {
    // Real logic only in a debug build - see DebugMenuGate's src/debug vs src/release variants.
    private val debugMenuGate = DebugMenuGate()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // TodoListApplication has already started Koin (with androidTodoDataModule included)
        // by the time any Activity runs - platformModules stays empty here rather than
        // registering that module a second time.
        setContent {
            App(
                platformModules = emptyList(),
                contentTabs = listOf(todoListTab())
            )
            debugMenuGate.Overlay()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            debugMenuGate.onVolumeKeyPressed()
        }
        return super.onKeyDown(keyCode, event)
    }
}
