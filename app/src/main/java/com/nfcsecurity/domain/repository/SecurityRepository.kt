package com.nfcsecurity.domain.repository

import com.nfcsecurity.domain.model.SecurityHealthScore
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    suspend fun runAllChecks(): SecurityHealthScore
    fun observeHealthScore(): Flow<SecurityHealthScore>
}
