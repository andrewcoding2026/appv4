package com.NFC.SecureShield.domain.usecase.session

import android.util.Base64
import com.NFC.SecureShield.data.local.KeystoreCryptoDataSource
import com.NFC.SecureShield.domain.model.FreemiumState
import com.NFC.SecureShield.domain.model.SessionToken
import com.NFC.SecureShield.domain.repository.SessionRepository
import com.NFC.SecureShield.domain.usecase.freemium.GetFreemiumStateUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateSessionUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var crypto: KeystoreCryptoDataSource
    private lateinit var getFreemiumState: GetFreemiumStateUseCase
    private lateinit var useCase: CreateSessionUseCase

    private val fakeSignatureBytes = ByteArray(32) { it.toByte() }
    private val fakeSignatureEncoded = "ZmFrZVNpZ25hdHVyZQ=="

    @Before
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } returns fakeSignatureEncoded

        sessionRepository = mockk()
        crypto = mockk()
        getFreemiumState = mockk()

        coEvery { crypto.sign(any()) } returns fakeSignatureBytes
        coJustRun { sessionRepository.saveSession(any()) }

        useCase = CreateSessionUseCase(sessionRepository, crypto, getFreemiumState)
    }

    @Test
    fun `Premium user gets session expiring in 365 days`() = runTest {
        coEvery { getFreemiumState() } returns FreemiumState.Premium

        val token = useCase()

        val expectedDurationMs = 365L * 24 * 60 * 60 * 1000
        val actualDuration = token.expiresAt - token.createdAt
        assertTrue(
            "Expected ~365 days, got $actualDuration ms",
            actualDuration in (expectedDurationMs - 1000)..(expectedDurationMs + 1000)
        )
    }

    @Test
    fun `Trial user gets session expiring at trial end`() = runTest {
        val remainingMs = 3 * 24 * 60 * 60 * 1000L
        coEvery { getFreemiumState() } returns FreemiumState.Trial(remainingMs)

        val token = useCase()

        val actualDuration = token.expiresAt - token.createdAt
        assertTrue(
            "Expected ~$remainingMs ms, got $actualDuration",
            actualDuration in (remainingMs - 1000)..(remainingMs + 1000)
        )
    }

    @Test
    fun `Expired user gets session already expired at creation`() = runTest {
        coEvery { getFreemiumState() } returns FreemiumState.Expired

        val token = useCase()

        assertTrue("expiresAt should be <= createdAt", token.expiresAt <= token.createdAt + 1000)
    }

    @Test
    fun `session is saved to repository`() = runTest {
        coEvery { getFreemiumState() } returns FreemiumState.Premium

        useCase()

        coVerify(exactly = 1) { sessionRepository.saveSession(any()) }
    }

    @Test
    fun `returned token has non-blank UUID-format token field`() = runTest {
        coEvery { getFreemiumState() } returns FreemiumState.Premium

        val token = useCase()

        assertNotNull(token.token)
        assertTrue(token.token.isNotBlank())
        // UUID format: 8-4-4-4-12 hex chars
        assertTrue(token.token.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }

    @Test
    fun `token signature is set from crypto sign output`() = runTest {
        coEvery { getFreemiumState() } returns FreemiumState.Premium

        val token = useCase()

        assertEquals(fakeSignatureEncoded, token.signature)
    }

    @Test
    fun `createdAt is recent`() = runTest {
        val beforeMs = System.currentTimeMillis()
        coEvery { getFreemiumState() } returns FreemiumState.Premium

        val token = useCase()

        val afterMs = System.currentTimeMillis()
        assertTrue(token.createdAt in beforeMs..afterMs)
    }
}
