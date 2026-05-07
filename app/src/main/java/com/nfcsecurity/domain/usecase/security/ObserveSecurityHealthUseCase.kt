package com.nfcsecurity.domain.usecase.security

import com.nfcsecurity.domain.model.SecurityHealthScore
import com.nfcsecurity.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSecurityHealthUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    operator fun invoke(): Flow<SecurityHealthScore> = repository.observeHealthScore()
}
