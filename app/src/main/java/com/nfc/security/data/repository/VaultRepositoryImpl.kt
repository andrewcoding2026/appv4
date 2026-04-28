package com.nfc.security.data.repository

import com.nfc.security.data.crypto.AesGcm
import com.nfc.security.data.db.VaultItemDao
import com.nfc.security.data.db.VaultItemEntity
import com.nfc.security.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VaultRepositoryImpl @Inject constructor(
    private val dao: VaultItemDao,
    private val aesGcm: AesGcm,
) : VaultRepository {

    override fun observeAll(): Flow<List<VaultItemEntity>> = dao.observeAll()

    override suspend fun addItem(label: String, type: String, plaintext: ByteArray): Long {
        val (ciphertext, iv) = aesGcm.encrypt(plaintext)
        val now = System.currentTimeMillis()
        return dao.insert(
            VaultItemEntity(
                label = label,
                type = type,
                ciphertext = ciphertext,
                iv = iv,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    override suspend fun deleteItem(id: Long) { dao.deleteById(id) }

    override suspend fun decryptItem(id: Long): ByteArray? {
        val item = dao.getById(id) ?: return null
        return aesGcm.decrypt(item.ciphertext, item.iv)
    }
}
