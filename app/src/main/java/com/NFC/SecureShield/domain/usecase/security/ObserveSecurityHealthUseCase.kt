package com.NFC.SecureShield.domain.usecase.security

import com.NFC.SecureShield.domain.model.SecurityHealthScore
import com.NFC.SecureShield.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSecurityHealthUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    operator fun invoke(): Flow<SecurityHealthScore> = repository.observeHealthScore()
}
