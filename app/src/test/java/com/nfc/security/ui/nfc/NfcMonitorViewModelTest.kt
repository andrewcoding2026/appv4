package com.nfc.security.ui.nfc

import app.cash.turbine.test
import com.nfc.security.domain.model.NfcTagInfo
import com.nfc.security.domain.model.NfcTagType
import com.nfc.security.domain.repository.NfcRepository
import com.nfc.security.domain.usecase.nfc.ObserveNfcStateUseCase
import com.nfc.security.util.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
class NfcMonitorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var observeNfcState: ObserveNfcStateUseCase
    private lateinit var nfcRepository: NfcRepository
    private lateinit var viewModel: NfcMonitorViewModel

    @Before
    fun setUp() {
        observeNfcState = mockk()
        nfcRepository = mockk()
    }

    @Test
    fun `initial state has NFC disabled and empty history`() = runTest {
        every { observeNfcState() } returns flowOf(false)
        every { nfcRepository.getLastDiscoveredTag() } returns flowOf(null)
        viewModel = NfcMonitorViewModel(observeNfcState, nfcRepository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isNfcEnabled)
        assertNull(viewModel.uiState.value.lastTag)
        assertTrue(viewModel.uiState.value.tagHistory.isEmpty())
    }

    @Test
    fun `isNfcEnabled updates when observe flow emits true`() = runTest {
        every { observeNfcState() } returns flowOf(true)
        every { nfcRepository.getLastDiscoveredTag() } returns flowOf(null)
        viewModel = NfcMonitorViewModel(observeNfcState, nfcRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isNfcEnabled)
    }

    @Test
    fun `lastTag and tagHistory update when repository emits a tag`() = runTest {
        val tag = fakeTag("AABBCCDD")
        every { observeNfcState() } returns flowOf(true)
        every { nfcRepository.getLastDiscoveredTag() } returns flowOf(tag)
        viewModel = NfcMonitorViewModel(observeNfcState, nfcRepository)
        advanceUntilIdle()

        assertEquals(tag, viewModel.uiState.value.lastTag)
        assertEquals(listOf(tag), viewModel.uiState.value.tagHistory)
    }

    @Test
    fun `null tag emission from repository is ignored`() = runTest {
        every { observeNfcState() } returns flowOf(false)
        every { nfcRepository.getLastDiscoveredTag() } returns flowOf(null)
        viewModel = NfcMonitorViewModel(observeNfcState, nfcRepository)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.lastTag)
        assertTrue(viewModel.uiState.value.tagHistory.isEmpty())
    }

    @Test
    fun `onNewTagDiscovered publishes tag to repository`() = runTest {
        every { observeNfcState() } returns flowOf(false)
        every { nfcRepository.getLastDiscoveredTag() } returns flowOf(null)
        coJustRun { nfcRepository.publishTag(any()) }
        viewModel = NfcMonitorViewModel(observeNfcState, nfcRepository)
        advanceUntilIdle()

        val newTag = fakeTag("11223344")
        viewModel.onNewTagDiscovered(newTag)
        advanceUntilIdle()

        coVerify(exactly = 1) { nfcRepository.publishTag(newTag) }
    }

    @Test
    fun `tagHistory is prepended with newest tag first`() = runTest {
        val tagFlow = MutableSharedFlow<NfcTagInfo?>(replay = 1)
        every { observeNfcState() } returns flowOf(false)
        every { nfcRepository.getLastDiscoveredTag() } returns tagFlow
        viewModel = NfcMonitorViewModel(observeNfcState, nfcRepository)

        val tag1 = fakeTag("0001")
        val tag2 = fakeTag("0002")

        viewModel.uiState.test {
            awaitItem() // initial

            tagFlow.emit(tag1)
            val state1 = awaitItem()
            assertEquals(tag1, state1.tagHistory.first())

            tagFlow.emit(tag2)
            val state2 = awaitItem()
            assertEquals(tag2, state2.tagHistory.first())
            assertEquals(tag1, state2.tagHistory[1])

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tagHistory is capped at 20 entries`() = runTest {
        val tagFlow = MutableSharedFlow<NfcTagInfo?>(replay = 1)
        every { observeNfcState() } returns flowOf(false)
        every { nfcRepository.getLastDiscoveredTag() } returns tagFlow
        viewModel = NfcMonitorViewModel(observeNfcState, nfcRepository)
        advanceUntilIdle()

        repeat(25) { index ->
            tagFlow.emit(fakeTag("%04X".format(index)))
            advanceUntilIdle()
        }

        assertTrue(viewModel.uiState.value.tagHistory.size <= 20)
    }

    private fun fakeTag(id: String) = NfcTagInfo(
        id = id,
        techList = listOf("NfcA"),
        type = NfcTagType.Iso14443A,
        ndefRecords = emptyList(),
        discoveredAt = System.currentTimeMillis()
    )
}
