package com.nfc.security.data.repository

import com.nfc.security.data.local.SecurityCheckDataSource
import com.nfc.security.domain.model.SecurityCheckResult
import com.nfc.security.domain.model.SecurityCheckResult.Severity
import com.nfc.security.domain.model.SecurityHealthScore
import com.nfc.security.domain.repository.SecurityRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    private val checkDataSource: SecurityCheckDataSource
) : SecurityRepository {

    override suspend fun runAllChecks(): SecurityHealthScore {
        val checks = listOf(
            checkDataSource.checkIsRooted(),
            checkDataSource.checkDeveloperOptions(),
            checkDataSource.checkUsbDebugging(),
            checkDataSource.checkScreenLock(),
            checkDataSource.checkBiometricAvailability(),
            checkDataSource.checkUnknownSourceApps(),
            checkDataSource.checkGooglePlayProtect()
        )
        val score = calculateScore(checks)
        return SecurityHealthScore(score = score, checks = checks, calculatedAt = System.currentTimeMillis())
    }

    override fun observeHealthScore(): Flow<SecurityHealthScore> = flow {
        while (true) {
            emit(runAllChecks())
            delay(5 * 60 * 1000L)
        }
    }

    private fun calculateScore(checks: List<SecurityCheckResult>): Int {
        val weights = mapOf(
            Severity.CRITICAL to 20,
            Severity.HIGH to 10,
            Severity.MEDIUM to 5,
            Severity.LOW to 2
        )
        val totalPossible = checks.sumOf { weights[it.severity] ?: 0 }
        if (totalPossible == 0) return 100
        val earned = checks.filter { it.passed }.sumOf { weights[it.severity] ?: 0 }
        return ((earned.toFloat() / totalPossible) * 100).toInt().coerceIn(0, 100)
    }
}
