package com.nfcsecurity.domain.repository

import com.nfcsecurity.domain.model.VpnState
import kotlinx.coroutines.flow.Flow

interface VpnRepository {
    fun observeVpnState(): Flow<VpnState>
    suspend fun updateVpnState(state: VpnState)
}
