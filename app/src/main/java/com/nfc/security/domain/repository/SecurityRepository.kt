package com.nfc.security.domain.repository

import com.nfc.security.domain.model.SecurityHealthScore
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun runAllChecks(): SecurityHealthScore
    fun observeHealthScore(): Flow<SecurityHealthScore>
}
