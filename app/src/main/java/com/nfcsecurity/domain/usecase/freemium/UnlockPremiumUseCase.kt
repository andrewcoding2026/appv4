package com.nfcsecurity.domain.usecase.freemium

import com.nfcsecurity.domain.repository.FreemiumRepository
import javax.inject.Inject

class UnlockPremiumUseCase @Inject constructor(
    private val repository: FreemiumRepository
) {
    suspend operator fun invoke() = repository.setPremium()
}
