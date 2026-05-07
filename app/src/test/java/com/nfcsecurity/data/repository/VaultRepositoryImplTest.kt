package com.nfcsecurity.data.repository

import com.nfcsecurity.data.crypto.AesGcm
import com.nfcsecurity.data.db.VaultItemDao
import com.nfcsecurity.data.db.VaultItemEntity
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class VaultRepositoryImplTest {

    private lateinit var dao: VaultItemDao
    private lateinit var aesGcm: AesGcm
    private lateinit var repository: VaultRepositoryImpl

    private val fakePlaintext = "super-secret".toByteArray()
    private val fakeCiphertext = ByteArray(32) { (it + 10).toByte() }
    private val fakeIv = ByteArray(12) { it.toByte() }

    @Before
    fun setUp() {
        dao = mockk()
        aesGcm = mockk()
        repository = VaultRepositoryImpl(dao, aesGcm)
    }

    @Test
    fun `observeAll delegates to dao`() {
        val items = listOf(fakeEntity(1L))
        every { dao.observeAll() } returns flowOf(items)

        val flow = repository.observeAll()

        verify { dao.observeAll() }
    }

    @Test
    fun `addItem encrypts plaintext before storing`() = runTest {
        every { aesGcm.encrypt(fakePlaintext) } returns (fakeCiphertext to fakeIv)
        coEvery { dao.insert(any()) } returns 1L

        repository.addItem("label", "password", fakePlaintext)

        verify(exactly = 1) { aesGcm.encrypt(fakePlaintext) }
    }

    @Test
    fun `addItem stores entity with encrypted ciphertext and iv`() = runTest {
        every { aesGcm.encrypt(fakePlaintext) } returns (fakeCiphertext to fakeIv)
        val entitySlot = slot<VaultItemEntity>()
        coEvery { dao.insert(capture(entitySlot)) } returns 1L

        repository.addItem("My Label", "TOTP", fakePlaintext)

        val stored = entitySlot.captured
        assertEquals("My Label", stored.label)
        assertEquals("TOTP", stored.type)
        assertArrayEquals(fakeCiphertext, stored.ciphertext)
        assertArrayEquals(fakeIv, stored.iv)
    }

    @Test
    fun `addItem returns generated id from dao`() = runTest {
        every { aesGcm.encrypt(any()) } returns (fakeCiphertext to fakeIv)
        coEvery { dao.insert(any()) } returns 99L

        val id = repository.addItem("label", "type", fakePlaintext)

        assertEquals(99L, id)
    }

    @Test
    fun `deleteItem delegates to dao deleteById`() = runTest {
        coJustRun { dao.deleteById(42L) }

        repository.deleteItem(42L)

        coVerify(exactly = 1) { dao.deleteById(42L) }
    }

    @Test
    fun `decryptItem returns null when item not found`() = runTest {
        coEvery { dao.getById(999L) } returns null

        val result = repository.decryptItem(999L)

        assertNull(result)
    }

    @Test
    fun `decryptItem decrypts stored ciphertext with stored iv`() = runTest {
        val entity = fakeEntity(1L)
        coEvery { dao.getById(1L) } returns entity
        every { aesGcm.decrypt(entity.ciphertext, entity.iv) } returns fakePlaintext

        val result = repository.decryptItem(1L)

        assertArrayEquals(fakePlaintext, result)
    }

    @Test
    fun `decryptItem returns null when aesGcm decrypt returns null (corrupted data)`() = runTest {
        val entity = fakeEntity(1L)
        coEvery { dao.getById(1L) } returns entity
        every { aesGcm.decrypt(any(), any()) } returns null

        val result = repository.decryptItem(1L)

        assertNull(result)
    }

    private fun fakeEntity(id: Long) = VaultItemEntity(
        id = id,
        label = "label",
        type = "password",
        ciphertext = fakeCiphertext,
        iv = fakeIv,
        createdAt = 0L,
        updatedAt = 0L
    )
}
