package com.nfc.security.domain.usecase.security

import com.nfc.security.domain.model.SecurityHealthScore
import com.nfc.security.domain.repository.SecurityRepository
import javax.inject.Inject

class RunSecurityChecksUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    suspend operator fun invoke(): SecurityHealthScore = repository.runAllChecks()
}
