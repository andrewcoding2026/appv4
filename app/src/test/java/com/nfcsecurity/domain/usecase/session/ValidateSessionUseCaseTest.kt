package com.nfcsecurity.domain.usecase.session

import android.util.Base64
import com.nfcsecurity.data.local.KeystoreCryptoDataSource
import com.nfcsecurity.domain.model.SessionToken
import com.nfcsecurity.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateSessionUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var crypto: KeystoreCryptoDataSource
    private lateinit var useCase: ValidateSessionUseCase

    private val fakeDecodedSig = ByteArray(32) { it.toByte() }

    @Before
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } returns fakeDecodedSig

        sessionRepository = mockk()
        crypto = mockk()
        useCase = ValidateSessionUseCase(sessionRepository, crypto)
    }

    @Test
    fun `returns false when no session exists`() = runTest {
        coEvery { sessionRepository.getSession() } returns null

        assertFalse(useCase())
    }

    @Test
    fun `returns false when session is expired`() = runTest {
        val expiredSession = SessionToken(
            token = "some-uuid",
            createdAt = System.currentTimeMillis() - 2000,
            expiresAt = System.currentTimeMillis() - 1000,
            signature = "sig"
        )
        coEvery { sessionRepository.getSession() } returns expiredSession

        assertFalse(useCase())
    }

    @Test
    fun `returns true when session is valid and signature verifies`() = runTest {
        val validSession = SessionToken(
            token = "some-uuid",
            createdAt = System.currentTimeMillis() - 1000,
            expiresAt = System.currentTimeMillis() + 10_000,
            signature = "validSig"
        )
        coEvery { sessionRepository.getSession() } returns validSession
        coEvery { crypto.verify(any(), any()) } returns true

        assertTrue(useCase())
    }

    @Test
    fun `returns false when session is not expired but signature does not verify`() = runTest {
        val validSession = SessionToken(
            token = "some-uuid",
            createdAt = System.currentTimeMillis() - 1000,
            expiresAt = System.currentTimeMillis() + 10_000,
            signature = "badSig"
        )
        coEvery { sessionRepository.getSession() } returns validSession
        coEvery { crypto.verify(any(), any()) } returns false

        assertFalse(useCase())
    }

    @Test
    fun `returns false when session expires exactly now`() = runTest {
        val now = System.currentTimeMillis()
        val borderlineSession = SessionToken(
            token = "uuid",
            createdAt = now - 1000,
            expiresAt = now - 1, // just expired
            signature = "sig"
        )
        coEvery { sessionRepository.getSession() } returns borderlineSession

        assertFalse(useCase())
    }
}
