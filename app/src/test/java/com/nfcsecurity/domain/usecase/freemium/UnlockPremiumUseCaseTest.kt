package com.NFC.SecureShield.free.domain.usecase.freemium

import com.NFC.SecureShield.free.domain.repository.FreemiumRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UnlockPremiumUseCaseTest {

    private lateinit var repository: FreemiumRepository
    private lateinit var useCase: UnlockPremiumUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = UnlockPremiumUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository setPremium`() = runTest {
        coJustRun { repository.setPremium() }

        useCase()

        coVerify(exactly = 1) { repository.setPremium() }
    }
}