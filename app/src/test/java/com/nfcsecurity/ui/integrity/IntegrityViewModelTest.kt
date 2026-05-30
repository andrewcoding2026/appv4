package com.NFC.SecureShield.free.ui.integrity

import app.cash.turbine.test
import com.NFC.SecureShield.free.domain.model.SecurityHealthScore
import com.NFC.SecureShield.free.domain.usecase.security.ObserveSecurityHealthUseCase
import com.NFC.SecureShield.free.domain.usecase.security.RunSecurityChecksUseCase
import com.NFC.SecureShield.free.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IntegrityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var observeSecurityHealth: ObserveSecurityHealthUseCase
    private lateinit var runChecks: RunSecurityChecksUseCase
    private lateinit var viewModel: IntegrityViewModel

    private val passingScore = SecurityHealthScore(score = 90, checks = emptyList(), calculatedAt = 0L)
    private val failingScore = SecurityHealthScore(score = 30, checks = emptyList(), calculatedAt = 0L)

    @Before
    fun setUp() {
        observeSecurityHealth = mockk()
        runChecks = mockk()
    }

    @Test
    fun `initial isLoading is true before any flow emission`() = runTest {
        every { observeSecurityHealth() } returns flowOf()
        viewModel = IntegrityViewModel(observeSecurityHealth, runChecks)

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `initial score is null before any emission`() = runTest {
        every { observeSecurityHealth() } returns flowOf()
        viewModel = IntegrityViewModel(observeSecurityHealth, runChecks)

        assertNull(viewModel.uiState.value.score)
    }

    @Test
    fun `score and isLoading updated when health flow emits`() = runTest {
        every { observeSecurityHealth() } returns flowOf(passingScore)
        viewModel = IntegrityViewModel(observeSecurityHealth, runChecks)
        advanceUntilIdle()

        assertEquals(passingScore, viewModel.uiState.value.score)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `auditNow sets isLoading true then false`() = runTest {
        every { observeSecurityHealth() } returns flowOf(passingScore)
        coEvery { runChecks() } returns passingScore
        viewModel = IntegrityViewModel(observeSecurityHealth, runChecks)
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // current loaded state

            viewModel.auditNow()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val done = awaitItem()
            assertFalse(done.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `auditNow updates score from runChecks result`() = runTest {
        every { observeSecurityHealth() } returns flowOf(passingScore)
        coEvery { runChecks() } returns failingScore
        viewModel = IntegrityViewModel(observeSecurityHealth, runChecks)
        advanceUntilIdle()

        viewModel.auditNow()
        advanceUntilIdle()

        assertEquals(failingScore, viewModel.uiState.value.score)
    }

    @Test
    fun `auditNow calls runChecks exactly once`() = runTest {
        every { observeSecurityHealth() } returns flowOf(passingScore)
        coEvery { runChecks() } returns passingScore
        viewModel = IntegrityViewModel(observeSecurityHealth, runChecks)
        advanceUntilIdle()

        viewModel.auditNow()
        advanceUntilIdle()

        coVerify(exactly = 1) { runChecks() }
    }

    @Test
    fun `subsequent health score updates from observe flow are reflected`() = runTest {
        every { observeSecurityHealth() } returns kotlinx.coroutines.flow.flow {
            emit(passingScore)
            emit(failingScore)
        }
        viewModel = IntegrityViewModel(observeSecurityHealth, runChecks)

        viewModel.uiState.test {
            awaitItem() // initial
            awaitItem() // passingScore
            val updated = awaitItem()
            assertEquals(failingScore, updated.score)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
