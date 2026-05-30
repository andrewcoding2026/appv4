package com.NFC.SecureShield.domain.usecase.freemium

import com.NFC.SecureShield.domain.model.FreemiumState
import com.NFC.SecureShield.domain.repository.FreemiumRepository
import javax.inject.Inject

private const val TRIAL_DURATION_MS = 7 * 24 * 60 * 60 * 1000L

class GetFreemiumStateUseCase @Inject constructor(
    private val repository: FreemiumRepository
) {
    suspend operator fun invoke(): FreemiumState {
        if (repository.isPremium()) return FreemiumState.Premium
        val trialStart = repository.getTrialStartMs()
        if (trialStart == Long.MIN_VALUE) return FreemiumState.Expired
        if (trialStart == -1L) return FreemiumState.Expired
        val elapsed = System.currentTimeMillis() - trialStart
        val remaining = TRIAL_DURATION_MS - elapsed
        return if (remaining > 0) FreemiumState.Trial(remaining) else FreemiumState.Expired
    }
}
