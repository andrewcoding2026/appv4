package com.nfcsecurity.data.repository

import com.nfcsecurity.data.crypto.AesGcm
import com.nfcsecurity.data.db.VaultItemDao
import com.nfcsecurity.data.db.VaultItemEntity
import com.nfcsecurity.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import javax.crypto.Cipher
import javax.inject.Inject

class VaultRepositoryImpl @Inject constructor(
    private val dao: VaultItemDao,
    private val aesGcm: AesGcm,
) : VaultRepository {

    override fun observeAll(): Flow<List<VaultItemEntity>> = dao.observeAll()

    override suspend fun addItem(label: String, type: String, plaintext: ByteArray, encryptCipher: Cipher): Long {
        val (ciphertext, iv) = aesGcm.encryptWithCipher(encryptCipher, plaintext)
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

    override suspend fun decryptItem(id: Long, decryptCipher: Cipher): ByteArray? {
        val item = dao.getById(id) ?: return null
        return aesGcm.decryptWithCipher(decryptCipher, item.ciphertext)
    }
}