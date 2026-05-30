package com.NFC.SecureShield.domain.repository

import com.NFC.SecureShield.domain.model.SecurityHealthScore
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun runAllChecks(): SecurityHealthScore
    fun observeHealthScore(): Flow<SecurityHealthScore>
}
