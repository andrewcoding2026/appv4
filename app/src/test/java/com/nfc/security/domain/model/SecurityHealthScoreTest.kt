package com.nfc.security.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityHealthScoreTest {

    private fun score(value: Int) = SecurityHealthScore(
        score = value,
        checks = emptyList(),
        calculatedAt = 0L
    )

    @Test
    fun `isPassing returns true when score is exactly 60`() {
        assertTrue(score(60).isPassing())
    }

    @Test
    fun `isPassing returns true when score is above 60`() {
        assertTrue(score(100).isPassing())
        assertTrue(score(61).isPassing())
        assertTrue(score(99).isPassing())
    }

    @Test
    fun `isPassing returns false when score is below 60`() {
        assertFalse(score(59).isPassing())
        assertFalse(score(0).isPassing())
        assertFalse(score(1).isPassing())
    }

    @Test
    fun `isPassing returns false when score is zero`() {
        assertFalse(score(0).isPassing())
    }
}
