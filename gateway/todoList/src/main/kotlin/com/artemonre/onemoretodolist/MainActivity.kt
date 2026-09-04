package com.artemonre.onemoretodolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab

class MainActivity : ComponentActivity() {
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
        }
    }
}