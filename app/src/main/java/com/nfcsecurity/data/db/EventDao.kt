package com.nfcsecurity.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE read = 0 ORDER BY createdAt DESC")
    fun observeUnread(): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) FROM events WHERE read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("UPDATE events SET read = 1")
    suspend fun markAllRead(): Int

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("SELECT * FROM events WHERE createdAt >= :sinceMs ORDER BY createdAt ASC")
    fun observeSince(sinceMs: Long): Flow<List<EventEntity>>
}
