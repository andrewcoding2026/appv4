package com.nfc.security.ui.scan

import app.cash.turbine.test
import com.nfc.security.domain.model.ScanReport
import com.nfc.security.domain.usecase.scan.ClearAppCacheUseCase
import com.nfc.security.domain.usecase.scan.ScanForMalwareUseCase
import com.nfc.security.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var scanForMalware: ScanForMalwareUseCase
    private lateinit var clearAppCache: ClearAppCacheUseCase
    private lateinit var viewModel: ScanViewModel

    private val fakeReport = ScanReport(
        hits = emptyList(),
        scannedApps = 50,
        scannedFiles = 5,
        riskScore = 0,
        scanCompletedAt = 1000L
    )

    @Before
    fun setUp() {
        scanForMalware = mockk()
        clearAppCache = mockk()
        viewModel = ScanViewModel(scanForMalware, clearAppCache)
    }

    @Test
    fun `initial state is idle with no report or error`() = runTest {
        val state = viewModel.uiState.value

        assertEquals(false, state.isScanning)
        assertNull(state.report)
        assertNull(state.error)
        assertNull(state.clearedBytes)
    }

    @Test
    fun `onStartScan sets isScanning true then false on success`() = runTest {
        coEvery { scanForMalware() } returns fakeReport

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(false, initial.isScanning)

            viewModel.onStartScan()

            val scanning = awaitItem()
            assertTrue(scanning.isScanning)

            val done = awaitItem()
            assertEquals(false, done.isScanning)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onStartScan populates report on success`() = runTest {
        coEvery { scanForMalware() } returns fakeReport

        viewModel.onStartScan()
        advanceUntilIdle()

        assertEquals(fakeReport, viewModel.uiState.value.report)
    }

    @Test
    fun `onStartScan sets error message on exception`() = runTest {
        coEvery { scanForMalware() } throws RuntimeException("Network error")

        viewModel.onStartScan()
        advanceUntilIdle()

        assertEquals("Network error", viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isScanning)
    }

    @Test
    fun `onStartScan sets default error message when exception message is null`() = runTest {
        coEvery { scanForMalware() } throws RuntimeException()

        viewModel.onStartScan()
        advanceUntilIdle()

        assertEquals("Scan failed", viewModel.uiState.value.error)
    }

    @Test
    fun `onStartScan clears previous error before scanning`() = runTest {
        coEvery { scanForMalware() } returns fakeReport

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onStartScan()

            val scanning = awaitItem()
            assertNull(scanning.error) // error cleared when scan starts

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onClearCache sets clearedBytes on success`() = runTest {
        coEvery { clearAppCache() } returns 2_048_000L

        viewModel.onClearCache()
        advanceUntilIdle()

        assertEquals(2_048_000L, viewModel.uiState.value.clearedBytes)
    }

    @Test
    fun `onClearCache sets error on exception`() = runTest {
        coEvery { clearAppCache() } throws RuntimeException("Permission denied")

        viewModel.onClearCache()
        advanceUntilIdle()

        assertEquals("Failed to clear cache", viewModel.uiState.value.error)
    }

    @Test
    fun `dismissClearedNotice sets clearedBytes to null`() = runTest {
        coEvery { clearAppCache() } returns 512L

        viewModel.onClearCache()
        advanceUntilIdle()

        viewModel.dismissClearedNotice()

        assertNull(viewModel.uiState.value.clearedBytes)
    }

    @Test
    fun `second scan replaces first report`() = runTest {
        val firstReport = fakeReport.copy(scannedApps = 10)
        val secondReport = fakeReport.copy(scannedApps = 20)
        coEvery { scanForMalware() } returnsMany listOf(firstReport, secondReport)

        viewModel.onStartScan()
        advanceUntilIdle()
        viewModel.onStartScan()
        advanceUntilIdle()

        assertEquals(20, viewModel.uiState.value.report?.scannedApps)
    }
}
