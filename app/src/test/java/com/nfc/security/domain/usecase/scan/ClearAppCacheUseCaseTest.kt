package com.nfc.security.domain.usecase.scan

import com.nfc.security.domain.repository.ScanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClearAppCacheUseCaseTest {

    private lateinit var repository: ScanRepository
    private lateinit var useCase: ClearAppCacheUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ClearAppCacheUseCase(repository)
    }

    @Test
    fun `invoke returns bytes cleared from repository`() = runTest {
        coEvery { repository.clearCaches() } returns 1_024_000L

        val result = useCase()

        assertEquals(1_024_000L, result)
    }

    @Test
    fun `invoke delegates to repository clearCaches`() = runTest {
        coEvery { repository.clearCaches() } returns 0L

        useCase()

        coVerify(exactly = 1) { repository.clearCaches() }
    }

    @Test
    fun `invoke returns zero when cache is empty`() = runTest {
        coEvery { repository.clearCaches() } returns 0L

        assertEquals(0L, useCase())
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        coEvery { repository.clearCaches() } throws IllegalStateException("Cannot clear")

        var caught: Throwable? = null
        try {
            useCase()
        } catch (e: IllegalStateException) {
            caught = e
        }

        assertEquals("Cannot clear", caught?.message)
    }
}