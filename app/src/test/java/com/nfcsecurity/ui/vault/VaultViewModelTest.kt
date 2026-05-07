package com.nfcsecurity.ui.vault

import app.cash.turbine.test
import com.nfcsecurity.data.db.VaultItemEntity
import com.nfcsecurity.domain.repository.VaultRepository
import com.nfcsecurity.util.MainDispatcherRule
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var vaultRepository: VaultRepository
    private lateinit var viewModel: VaultViewModel

    @Before
    fun setUp() {
        vaultRepository = mockk()
        viewModel = VaultViewModel(vaultRepository)
    }

    @Test
    fun `initial state is Locked`() {
        assertTrue(viewModel.uiState.value is VaultUiState.Locked)
    }

    @Test
    fun `onUnlocked transitions through Unlocking to Unlocked`() = runTest {
        val items = listOf(fakeEntity(1L), fakeEntity(2L))
        every { vaultRepository.observeAll() } returns flowOf(items)

        viewModel.uiState.test {
            assertEquals(VaultUiState.Locked, awaitItem())

            viewModel.onUnlocked()

            assertEquals(VaultUiState.Unlocking, awaitItem())
            val unlocked = awaitItem() as VaultUiState.Unlocked
            assertEquals(items, unlocked.items)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onUnlocked with empty vault results in empty item list`() = runTest {
        every { vaultRepository.observeAll() } returns flowOf(emptyList())

        viewModel.onUnlocked()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is VaultUiState.Unlocked)
        assertTrue((state as VaultUiState.Unlocked).items.isEmpty())
    }

    @Test
    fun `lock transitions back to Locked from Unlocked`() = runTest {
        every { vaultRepository.observeAll() } returns flowOf(emptyList())

        viewModel.onUnlocked()
        advanceUntilIdle()

        viewModel.lock()

        assertTrue(viewModel.uiState.value is VaultUiState.Locked)
    }

    @Test
    fun `addItem calls repository with correct arguments`() = runTest {
        coJustRun { vaultRepository.addItem(any(), any(), any()) }

        viewModel.addItem("My Password", "password", "s3cr3t")
        advanceUntilIdle()

        coVerify(exactly = 1) {
            vaultRepository.addItem("My Password", "password", "s3cr3t".toByteArray())
        }
    }

    @Test
    fun `deleteItem calls repository with correct id`() = runTest {
        coJustRun { vaultRepository.deleteItem(any()) }

        viewModel.deleteItem(42L)
        advanceUntilIdle()

        coVerify(exactly = 1) { vaultRepository.deleteItem(42L) }
    }

    @Test
    fun `vault items update when repository flow emits new list`() = runTest {
        val firstBatch = listOf(fakeEntity(1L))
        val secondBatch = listOf(fakeEntity(1L), fakeEntity(2L))
        every { vaultRepository.observeAll() } returns kotlinx.coroutines.flow.flow {
            emit(firstBatch)
            emit(secondBatch)
        }

        viewModel.uiState.test {
            awaitItem() // Locked

            viewModel.onUnlocked()

            awaitItem() // Unlocking
            val first = awaitItem() as VaultUiState.Unlocked
            assertEquals(1, first.items.size)

            val second = awaitItem() as VaultUiState.Unlocked
            assertEquals(2, second.items.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun fakeEntity(id: Long) = VaultItemEntity(
        id = id,
        label = "label $id",
        type = "password",
        ciphertext = ByteArray(16),
        iv = ByteArray(12),
        createdAt = 0L,
        updatedAt = 0L
    )
}
