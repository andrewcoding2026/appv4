package com.NFC.SecureShield.free.domain.repository

import com.NFC.SecureShield.free.domain.model.SecurityHealthScore
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun runAllChecks(): SecurityHealthScore
    fun observeHealthScore(): Flow<SecurityHealthScore>
}
