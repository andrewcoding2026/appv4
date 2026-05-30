package com.NFC.SecureShield.data.repository

import com.NFC.SecureShield.data.db.EventDao
import com.NFC.SecureShield.data.db.EventEntity
import com.NFC.SecureShield.domain.repository.EventRepository
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val dao: EventDao
) : EventRepository {
    override fun observeAll() = dao.observeAll()
    override fun observeUnread() = dao.observeUnread()
    override fun observeUnreadCount() = dao.observeUnreadCount()
    override fun observeSince(sinceMs: Long) = dao.observeSince(sinceMs)
    override suspend fun insert(event: EventEntity) = dao.insert(event)
    override suspend fun getById(id: Long) = dao.getById(id)
    override suspend fun markAllRead() { dao.markAllRead() }
    override suspend fun deleteById(id: Long) { dao.deleteById(id) }
}
