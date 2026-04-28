package com.nfc.security.domain.repository

import com.nfc.security.data.db.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeAll(): Flow<List<EventEntity>>
    fun observeUnread(): Flow<List<EventEntity>>
    fun observeUnreadCount(): Flow<Int>
    fun observeSince(sinceMs: Long): Flow<List<EventEntity>>
    suspend fun insert(event: EventEntity): Long
    suspend fun getById(id: Long): EventEntity?
    suspend fun markAllRead()
    suspend fun deleteById(id: Long)
}
