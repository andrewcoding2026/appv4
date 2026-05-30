package com.NFC.SecureShield.free.domain.repository

import com.NFC.SecureShield.free.data.db.EventEntity
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
