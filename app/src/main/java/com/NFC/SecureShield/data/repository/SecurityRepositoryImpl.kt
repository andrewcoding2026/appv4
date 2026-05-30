package com.NFC.SecureShield.data.repository

import com.NFC.SecureShield.data.local.SecurityCheckDataSource
import com.NFC.SecureShield.domain.model.SecurityCheckResult
import com.NFC.SecureShield.domain.model.SecurityCheckResult.Severity
import com.NFC.SecureShield.domain.model.SecurityHealthScore
import com.NFC.SecureShield.domain.repository.SecurityRepository
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
        val checks = mutableListOf<SecurityCheckResult>()
        
        val checkFunctions = listOf(
            suspend { checkDataSource.checkIsRooted() },
            suspend { checkDataSource.checkDeveloperOptions() },
            suspend { checkDataSource.checkUsbDebugging() },
            suspend { checkDataSource.checkScreenLock() },
            suspend { checkDataSource.checkBiometricAvailability() },
            suspend { checkDataSource.checkUnknownSourceApps() },
            suspend { checkDataSource.checkGooglePlayProtect() },
            suspend { checkDataSource.checkPlayIntegrity() }
        )

        for (checkFn in checkFunctions) {
            try {
                checks.add(checkFn())
            } catch (e: Exception) {
                // Silently catch and add a failed check result to avoid crashing the whole app
                checks.add(SecurityCheckResult("Security Check", false, Severity.LOW, "Check failed due to internal error"))
            }
        }

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
