package com.NFC.SecureShield.free.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.NFC.SecureShield.free.domain.model.SecurityCheckResult.Severity
import com.NFC.SecureShield.free.domain.usecase.scan.ScanForMalwareUseCase
import com.NFC.SecureShield.free.domain.usecase.security.RunSecurityChecksUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SecurityWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val runSecurityChecks: RunSecurityChecksUseCase,
    private val scanForMalware: ScanForMalwareUseCase,
    private val notificationHelper: VpnNotificationHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "security_periodic_check"
    }

    override suspend fun doWork(): Result {
        return try {
            notificationHelper.createChannels()
            val healthScore = runSecurityChecks()
            val criticalFailed = healthScore.checks.filter {
                !it.passed && it.severity == Severity.CRITICAL
            }
            if (criticalFailed.isNotEmpty()) {
                notificationHelper.showSecurityAlert(
                    context,
                    "Critical Security Issue",
                    criticalFailed.first().detail
                )
            }
            val scanReport = scanForMalware()
            if (scanReport.hits.isNotEmpty()) {
                notificationHelper.showSecurityAlert(
                    context,
                    "Potential Threats Found",
                    "${scanReport.hits.size} issue(s) detected — open app to review"
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
