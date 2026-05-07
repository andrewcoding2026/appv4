package com.nfcsecurity.domain.usecase.security

import com.nfcsecurity.domain.model.SecurityHealthScore
import com.nfcsecurity.domain.repository.SecurityRepository
import javax.inject.Inject

class RunSecurityChecksUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    suspend operator fun invoke(): SecurityHealthScore = repository.runAllChecks()
}
