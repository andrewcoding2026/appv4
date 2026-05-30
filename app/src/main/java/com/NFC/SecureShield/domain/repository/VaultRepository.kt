package com.NFC.SecureShield.domain.repository

import com.NFC.SecureShield.data.db.VaultItemEntity
import kotlinx.coroutines.flow.Flow
import javax.crypto.Cipher

interface VaultRepository {
    fun observeAll(): Flow<List<VaultItemEntity>>
    suspend fun addItem(label: String, type: String, plaintext: ByteArray, encryptCipher: Cipher): Long
    suspend fun deleteItem(id: Long)
    suspend fun decryptItem(id: Long, decryptCipher: Cipher): ByteArray?
}