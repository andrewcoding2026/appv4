package com.NFC.SecureShield.free.domain.repository

import com.NFC.SecureShield.free.domain.model.VpnState
import kotlinx.coroutines.flow.Flow

interface VpnRepository {
    fun observeVpnState(): Flow<VpnState>
    suspend fun updateVpnState(state: VpnState)
}
