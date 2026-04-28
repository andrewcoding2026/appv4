package com.nfc.security.domain.repository

import com.nfc.security.data.db.VaultItemEntity
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun observeAll(): Flow<List<VaultItemEntity>>
    suspend fun addItem(label: String, type: String, plaintext: ByteArray): Long
    suspend fun deleteItem(id: Long)
    suspend fun decryptItem(id: Long): ByteArray?
}
