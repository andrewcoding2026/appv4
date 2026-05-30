package com.NFC.SecureShield.free.domain.model

data class SecurityHealthScore(
    val score: Int,
    val checks: List<SecurityCheckResult>,
    val calculatedAt: Long
) {
    fun isPassing(): Boolean = score >= 60
}
