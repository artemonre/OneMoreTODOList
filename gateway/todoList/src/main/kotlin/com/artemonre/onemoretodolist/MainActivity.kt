package com.artemonre.onemoretodolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.artemonre.onemoretodolist.feature.todolist.di.androidTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                platformModules = listOf(androidTodoDataModule(applicationContext)),
                contentTabs = listOf(todoListTab())
            )
        }
    }
}