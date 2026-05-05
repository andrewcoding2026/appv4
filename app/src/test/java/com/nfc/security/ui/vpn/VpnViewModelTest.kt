package com.nfc.security.ui.vpn

import android.content.Intent
import app.cash.turbine.test
import com.nfc.security.domain.model.VpnState
import com.nfc.security.domain.usecase.vpn.ObserveVpnStateUseCase
import com.nfc.security.domain.usecase.vpn.StartVpnUseCase
import com.nfc.security.domain.usecase.vpn.StopVpnUseCase
import com.nfc.security.util.MainDispatcherRule
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
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
class VpnViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var observeVpnState: ObserveVpnStateUseCase
    private lateinit var startVpn: StartVpnUseCase
    private lateinit var stopVpn: StopVpnUseCase
    private lateinit var viewModel: VpnViewModel

    @Before
    fun setUp() {
        observeVpnState = mockk()
        startVpn = mockk()
        stopVpn = mockk()
    }

    @Test
    fun `initial VPN state is Disconnected`() = runTest {
        every { observeVpnState() } returns flowOf(VpnState.Disconnected)
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)

        assertEquals(VpnState.Disconnected, viewModel.uiState.value.vpnState)
    }

    @Test
    fun `vpnState updates when observe flow emits Connected`() = runTest {
        val connected = VpnState.Connected(serverIp = "10.0.0.1", bytesIn = 1024L, bytesOut = 512L)
        every { observeVpnState() } returns flowOf(connected)
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)
        advanceUntilIdle()

        assertEquals(connected, viewModel.uiState.value.vpnState)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `onConnectClick emits RequestVpnPermission event when prepareIntent is non-null`() = runTest {
        val permissionIntent = mockk<Intent>()
        every { observeVpnState() } returns flowOf(VpnState.Disconnected)
        every { startVpn.prepareIntent() } returns permissionIntent
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onConnectClick()
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is VpnEvent.RequestVpnPermission)
            assertEquals(permissionIntent, (event as VpnEvent.RequestVpnPermission).intent)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onConnectClick calls start directly when no permission needed`() = runTest {
        every { observeVpnState() } returns flowOf(VpnState.Disconnected)
        every { startVpn.prepareIntent() } returns null
        justRun { startVpn.start() }
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)
        advanceUntilIdle()

        viewModel.onConnectClick()
        advanceUntilIdle()

        verify(exactly = 1) { startVpn.start() }
    }

    @Test
    fun `onConnectClick sets isLoading true then false when permission dialog shown`() = runTest {
        val permissionIntent = mockk<Intent>()
        every { observeVpnState() } returns flowOf(VpnState.Disconnected)
        every { startVpn.prepareIntent() } returns permissionIntent
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem() // current state

            viewModel.onConnectClick()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val notLoading = awaitItem()
            assertFalse(notLoading.isLoading)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onVpnPermissionGranted calls start`() = runTest {
        every { observeVpnState() } returns flowOf(VpnState.Disconnected)
        justRun { startVpn.start() }
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)

        viewModel.onVpnPermissionGranted()

        verify(exactly = 1) { startVpn.start() }
    }

    @Test
    fun `onDisconnectClick calls stopVpn`() = runTest {
        every { observeVpnState() } returns flowOf(VpnState.Disconnected)
        justRun { stopVpn() }
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)

        viewModel.onDisconnectClick()

        verify(exactly = 1) { stopVpn() }
    }

    @Test
    fun `vpnState reflects Error from observe flow`() = runTest {
        val error = VpnState.Error("Connection refused")
        every { observeVpnState() } returns flowOf(error)
        viewModel = VpnViewModel(observeVpnState, startVpn, stopVpn)
        advanceUntilIdle()

        assertEquals(error, viewModel.uiState.value.vpnState)
    }
}
