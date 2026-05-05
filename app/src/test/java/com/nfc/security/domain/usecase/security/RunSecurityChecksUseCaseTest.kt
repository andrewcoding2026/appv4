package com.nfc.security.domain.usecase.security

import com.nfc.security.domain.model.SecurityHealthScore
import com.nfc.security.domain.repository.SecurityRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RunSecurityChecksUseCaseTest {

    private lateinit var repository: SecurityRepository
    private lateinit var useCase: RunSecurityChecksUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = RunSecurityChecksUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository runAllChecks`() = runTest {
        val expected = SecurityHealthScore(score = 85, checks = emptyList(), calculatedAt = 0L)
        coEvery { repository.runAllChecks() } returns expected

        val result = useCase()

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.runAllChecks() }
    }

    @Test
    fun `invoke returns failing score from repository`() = runTest {
        val failingScore = SecurityHealthScore(score = 40, checks = emptyList(), calculatedAt = 0L)
        coEvery { repository.runAllChecks() } returns failingScore

        val result = useCase()

        assertEquals(40, result.score)
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        coEvery { repository.runAllChecks() } throws RuntimeException("Check failed")

        var caught: String? = null
        try {
            useCase()
        } catch (e: RuntimeException) {
            caught = e.message
        }

        assertEquals("Check failed", caught)
    }
}