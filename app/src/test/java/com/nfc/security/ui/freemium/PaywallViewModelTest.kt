package com.nfc.security.ui.freemium

import app.cash.turbine.test
import com.nfc.security.domain.model.FreemiumState
import com.nfc.security.domain.usecase.freemium.ObserveFreemiumStateUseCase
import com.nfc.security.domain.usecase.freemium.UnlockPremiumUseCase
import com.nfc.security.util.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var observeFreemiumState: ObserveFreemiumStateUseCase
    private lateinit var unlockPremium: UnlockPremiumUseCase
    private lateinit var viewModel: PaywallViewModel

    @Before
    fun setUp() {
        observeFreemiumState = mockk()
        unlockPremium = mockk()
    }

    @Test
    fun `initial state reflects Expired freemium from repository`() = runTest {
        every { observeFreemiumState() } returns flowOf(FreemiumState.Expired)
        viewModel = PaywallViewModel(observeFreemiumState, unlockPremium)
        advanceUntilIdle()

        assertEquals(FreemiumState.Expired, viewModel.uiState.value.freemiumState)
    }

    @Test
    fun `state updates when repository emits Trial state`() = runTest {
        val trialState = FreemiumState.Trial(3 * 24 * 60 * 60 * 1000L)
        every { observeFreemiumState() } returns flowOf(trialState)
        viewModel = PaywallViewModel(observeFreemiumState, unlockPremium)
        advanceUntilIdle()

        assertEquals(trialState, viewModel.uiState.value.freemiumState)
    }

    @Test
    fun `state updates when repository emits Premium state`() = runTest {
        every { observeFreemiumState() } returns flowOf(FreemiumState.Premium)
        viewModel = PaywallViewModel(observeFreemiumState, unlockPremium)
        advanceUntilIdle()

        assertEquals(FreemiumState.Premium, viewModel.uiState.value.freemiumState)
    }

    @Test
    fun `onUnlockPremium sets isUnlocking true during operation then false after`() = runTest {
        every { observeFreemiumState() } returns flowOf(FreemiumState.Expired)
        coJustRun { unlockPremium() }
        viewModel = PaywallViewModel(observeFreemiumState, unlockPremium)
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // current state

            viewModel.onUnlockPremium()

            val unlocking = awaitItem()
            assertTrue(unlocking.isUnlocking)

            val done = awaitItem()
            assertFalse(done.isUnlocking)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onUnlockPremium calls unlock use case`() = runTest {
        every { observeFreemiumState() } returns flowOf(FreemiumState.Expired)
        coJustRun { unlockPremium() }
        viewModel = PaywallViewModel(observeFreemiumState, unlockPremium)
        advanceUntilIdle()

        viewModel.onUnlockPremium()
        advanceUntilIdle()

        coVerify(exactly = 1) { unlockPremium() }
    }

    @Test
    fun `freemiumState updates sequentially as flow emits`() = runTest {
        val trialState = FreemiumState.Trial(100_000L)
        every { observeFreemiumState() } returns kotlinx.coroutines.flow.flow {
            emit(trialState)
            emit(FreemiumState.Premium)
        }
        viewModel = PaywallViewModel(observeFreemiumState, unlockPremium)

        viewModel.uiState.test {
            awaitItem() // initial default
            val afterTrial = awaitItem()
            assertEquals(trialState, afterTrial.freemiumState)

            val afterPremium = awaitItem()
            assertEquals(FreemiumState.Premium, afterPremium.freemiumState)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
