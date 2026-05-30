package com.NFC.SecureShield.free.domain.usecase.security

import com.NFC.SecureShield.free.domain.model.SecurityHealthScore
import com.NFC.SecureShield.free.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSecurityHealthUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    operator fun invoke(): Flow<SecurityHealthScore> = repository.observeHealthScore()
}
