package com.nfc.security.domain.usecase.freemium

import com.nfc.security.domain.repository.FreemiumRepository
import javax.inject.Inject

class UnlockPremiumUseCase @Inject constructor(
    private val repository: FreemiumRepository
) {
    suspend operator fun invoke() = repository.setPremium()
}
