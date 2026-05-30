package com.NFC.SecureShield.free.domain.usecase.security

import com.NFC.SecureShield.free.domain.model.SecurityHealthScore
import com.NFC.SecureShield.free.domain.repository.SecurityRepository
import javax.inject.Inject

class RunSecurityChecksUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    suspend operator fun invoke(): SecurityHealthScore = repository.runAllChecks()
}
