package com.nfc.security.domain.repository

import com.nfc.security.domain.model.VpnState
import kotlinx.coroutines.flow.Flow

interface VpnRepository {
    fun observeVpnState(): Flow<VpnState>
    suspend fun updateVpnState(state: VpnState)
}
