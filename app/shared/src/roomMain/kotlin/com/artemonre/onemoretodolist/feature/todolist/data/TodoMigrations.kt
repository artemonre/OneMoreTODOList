package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.RenameColumn
import androidx.room3.migration.AutoMigrationSpec

@RenameColumn(tableName = "todo_items", fromColumnName = "title", toColumnName = "text")
class RenameTodoTitleToText : AutoMigrationSpec
