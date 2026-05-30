package com.NFC.SecureShield.free.ui.dashboard

import app.cash.turbine.test
import com.NFC.SecureShield.free.data.db.EventEntity
import com.NFC.SecureShield.free.domain.model.FreemiumState
import com.NFC.SecureShield.free.domain.model.SecurityHealthScore
import com.NFC.SecureShield.free.domain.model.VpnState
import com.NFC.SecureShield.free.domain.repository.EventRepository
import com.NFC.SecureShield.free.domain.usecase.freemium.ObserveFreemiumStateUseCase
import com.NFC.SecureShield.free.domain.usecase.nfc.ObserveNfcStateUseCase
import com.NFC.SecureShield.free.domain.usecase.security.ObserveSecurityHealthUseCase
import com.NFC.SecureShield.free.domain.usecase.vpn.ObserveVpnStateUseCase
import com.NFC.SecureShield.free.util.MainDispatcherRule
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
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var observeNfcState: ObserveNfcStateUseCase
    private lateinit var observeVpnState: ObserveVpnStateUseCase
    private lateinit var observeSecurityHealth: ObserveSecurityHealthUseCase
    private lateinit var observeFreemiumState: ObserveFreemiumStateUseCase
    private lateinit var eventRepository: EventRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        observeNfcState = mockk()
        observeVpnState = mockk()
        observeSecurityHealth = mockk()
        observeFreemiumState = mockk()
        eventRepository = mockk()
    }

    private fun buildViewModel() {
        viewModel = DashboardViewModel(
            observeNfcState,
            observeVpnState,
            observeSecurityHealth,
            observeFreemiumState,
            eventRepository
        )
    }

    @Test
    fun `initial isLoading is true`() = runTest {
        every { observeNfcState() } returns flowOf()
        every { observeVpnState() } returns flowOf()
        every { observeSecurityHealth() } returns flowOf()
        every { observeFreemiumState() } returns flowOf()
        every { eventRepository.observeUnreadCount() } returns flowOf()
        every { eventRepository.observeSince(any()) } returns flowOf()

        buildViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `isLoading becomes false after all flows emit`() = runTest {
        stubAllFlows(
            nfc = true,
            vpn = VpnState.Disconnected,
            health = SecurityHealthScore(80, emptyList(), 0L),
            freemium = FreemiumState.Premium
        )
        buildViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `nfcEnabled state reflects observable`() = runTest {
        stubAllFlows(nfc = true)
        buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.nfcEnabled)
    }

    @Test
    fun `vpnState reflects Connected from observable`() = runTest {
        val connected = VpnState.Connected("10.0.0.1", 0, 0)
        stubAllFlows(vpn = connected)
        buildViewModel()
        advanceUntilIdle()

        assertEquals(connected, viewModel.uiState.value.vpnState)
    }

    @Test
    fun `healthScore reflects observable value`() = runTest {
        val score = SecurityHealthScore(score = 95, checks = emptyList(), calculatedAt = 0L)
        stubAllFlows(health = score)
        buildViewModel()
        advanceUntilIdle()

        assertEquals(score, viewModel.uiState.value.healthScore)
    }

    @Test
    fun `freemiumState reflects Premium from observable`() = runTest {
        stubAllFlows(freemium = FreemiumState.Premium)
        buildViewModel()
        advanceUntilIdle()

        assertEquals(FreemiumState.Premium, viewModel.uiState.value.freemiumState)
    }

    @Test
    fun `unreadCount updates from event repository`() = runTest {
        stubAllFlows()
        every { eventRepository.observeUnreadCount() } returns flowOf(7)
        buildViewModel()
        advanceUntilIdle()

        assertEquals(7, viewModel.uiState.value.unreadCount)
    }

    @Test
    fun `sparklineData has 24 buckets`() = runTest {
        stubAllFlows()
        every { eventRepository.observeSince(any()) } returns flowOf(emptyList())
        buildViewModel()
        advanceUntilIdle()

        assertEquals(24, viewModel.uiState.value.sparklineData.size)
    }

    @Test
    fun `sparklineData buckets event from 2 hours ago into correct slot`() = runTest {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - TimeUnit.HOURS.toMillis(2)
        val event = fakeEvent(twoHoursAgo)

        stubAllFlows()
        every { eventRepository.observeSince(any()) } returns flowOf(listOf(event))
        buildViewModel()
        advanceUntilIdle()

        // Bucket index 23 = now, 22 = 1h ago, 21 = 2h ago
        assertEquals(1, viewModel.uiState.value.sparklineData[21])
    }

    @Test
    fun `sparklineData all zeros when no events`() = runTest {
        stubAllFlows()
        every { eventRepository.observeSince(any()) } returns flowOf(emptyList())
        buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.sparklineData.all { it == 0 })
    }

    @Test
    fun `sparklineData event from current hour lands in last bucket`() = runTest {
        val now = System.currentTimeMillis()
        val event = fakeEvent(now - 60_000) // 1 minute ago

        stubAllFlows()
        every { eventRepository.observeSince(any()) } returns flowOf(listOf(event))
        buildViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.sparklineData[23])
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun stubAllFlows(
        nfc: Boolean = false,
        vpn: VpnState = VpnState.Disconnected,
        health: SecurityHealthScore = SecurityHealthScore(0, emptyList(), 0L),
        freemium: FreemiumState = FreemiumState.Expired
    ) {
        every { observeNfcState() } returns flowOf(nfc)
        every { observeVpnState() } returns flowOf(vpn)
        every { observeSecurityHealth() } returns flowOf(health)
        every { observeFreemiumState() } returns flowOf(freemium)
        every { eventRepository.observeUnreadCount() } returns flowOf(0)
        every { eventRepository.observeSince(any()) } returns flowOf(emptyList())
    }

    private fun fakeEvent(createdAt: Long) = EventEntity(
        id = 1L,
        module = "nfc",
        severity = "HIGH",
        title = "Tag detected",
        body = "",
        createdAt = createdAt
    )
}
