package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items WHERE deletedAt IS NULL")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todo_items")
    suspend fun getAllIncludingDeleted(): List<TodoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TodoEntity)

    // One statement for the whole list, so Room runs it in a single transaction.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TodoEntity>)

    // Instants are stored as epoch millis, see TodoDateConverters.
    @Query("UPDATE todo_items SET deletedAt = :nowMillis, updatedAt = :nowMillis WHERE id = :id")
    suspend fun markDeleted(id: String, nowMillis: Long)

    @Query("DELETE FROM todo_items WHERE deletedAt IS NOT NULL AND deletedAt < :cutoffMillis")
    suspend fun purgeDeletedBefore(cutoffMillis: Long)
}
