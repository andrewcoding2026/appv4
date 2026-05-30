package com.NFC.SecureShield.domain.usecase.freemium

import com.NFC.SecureShield.domain.model.FreemiumState
import com.NFC.SecureShield.domain.repository.FreemiumRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private const val TRIAL_DURATION_MS = 7 * 24 * 60 * 60 * 1000L

class GetFreemiumStateUseCaseTest {

    private lateinit var repository: FreemiumRepository
    private lateinit var useCase: GetFreemiumStateUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetFreemiumStateUseCase(repository)
    }

    @Test
    fun `returns Premium when isPremium is true`() = runTest {
        coEvery { repository.isPremium() } returns true

        val result = useCase()

        assertEquals(FreemiumState.Premium, result)
    }

    @Test
    fun `returns Expired when trialStart is Long MIN_VALUE`() = runTest {
        coEvery { repository.isPremium() } returns false
        coEvery { repository.getTrialStartMs() } returns Long.MIN_VALUE

        val result = useCase()

        assertEquals(FreemiumState.Expired, result)
    }

    @Test
    fun `returns Expired when trialStart is -1`() = runTest {
        coEvery { repository.isPremium() } returns false
        coEvery { repository.getTrialStartMs() } returns -1L

        val result = useCase()

        assertEquals(FreemiumState.Expired, result)
    }

    @Test
    fun `returns Trial with remaining time when trial started 3 days ago`() = runTest {
        val threeDaysMs = 3 * 24 * 60 * 60 * 1000L
        val trialStart = System.currentTimeMillis() - threeDaysMs
        coEvery { repository.isPremium() } returns false
        coEvery { repository.getTrialStartMs() } returns trialStart

        val result = useCase()

        assertTrue(result is FreemiumState.Trial)
        val remaining = (result as FreemiumState.Trial).remainingMs
        assertTrue(remaining > 0)
        assertTrue(remaining <= TRIAL_DURATION_MS - threeDaysMs + 1000)
    }

    @Test
    fun `returns Expired when trial started 8 days ago`() = runTest {
        val eightDaysMs = 8 * 24 * 60 * 60 * 1000L
        val trialStart = System.currentTimeMillis() - eightDaysMs
        coEvery { repository.isPremium() } returns false
        coEvery { repository.getTrialStartMs() } returns trialStart

        val result = useCase()

        assertEquals(FreemiumState.Expired, result)
    }

    @Test
    fun `returns Expired when remaining time is exactly zero`() = runTest {
        val trialStart = System.currentTimeMillis() - TRIAL_DURATION_MS
        coEvery { repository.isPremium() } returns false
        coEvery { repository.getTrialStartMs() } returns trialStart

        val result = useCase()

        assertEquals(FreemiumState.Expired, result)
    }

    @Test
    fun `returns Trial with full duration when trial just started`() = runTest {
        val trialStart = System.currentTimeMillis()
        coEvery { repository.isPremium() } returns false
        coEvery { repository.getTrialStartMs() } returns trialStart

        val result = useCase()

        assertTrue(result is FreemiumState.Trial)
        val remaining = (result as FreemiumState.Trial).remainingMs
        assertTrue(remaining > TRIAL_DURATION_MS - 1000)
    }

    @Test
    fun `isPremium check short-circuits trial calculation`() = runTest {
        coEvery { repository.isPremium() } returns true

        val result = useCase()

        // getTrialStartMs should never be called for premium users
        assertEquals(FreemiumState.Premium, result)
    }
}
