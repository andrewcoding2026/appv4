package com.nfcsecurity.domain.model

data class SecurityCheckResult(
    val checkName: String,
    val passed: Boolean,
    val severity: Severity,
    val detail: String
) {
    enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }
}
