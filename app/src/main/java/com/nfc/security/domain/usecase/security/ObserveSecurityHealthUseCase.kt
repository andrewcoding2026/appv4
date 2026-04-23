package com.nfc.security.domain.usecase.security

import com.nfc.security.domain.model.SecurityHealthScore
import com.nfc.security.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSecurityHealthUseCase @Inject constructor(
    private val repository: SecurityRepository
) {
    operator fun invoke(): Flow<SecurityHealthScore> = repository.observeHealthScore()
}
