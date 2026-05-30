package com.NFC.SecureShield.domain.usecase.security

import com.NFC.SecureShield.domain.model.SecurityHealthScore
import com.NFC.SecureShield.domain.repository.SecurityRepository
import javax.inject.Inject

class RunSecurityChecksUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    suspend operator fun invoke(): SecurityHealthScore = repository.runAllChecks()
}
