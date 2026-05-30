package com.NFC.SecureShield.domain.usecase.freemium

import com.NFC.SecureShield.domain.repository.FreemiumRepository
import javax.inject.Inject

class UnlockPremiumUseCase @Inject constructor(
    private val repository: FreemiumRepository
) {
    suspend operator fun invoke() = repository.setPremium()
}
