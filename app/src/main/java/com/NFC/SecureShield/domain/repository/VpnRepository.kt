package com.NFC.SecureShield.domain.repository

import com.NFC.SecureShield.domain.model.VpnState
import kotlinx.coroutines.flow.Flow

interface VpnRepository {
    fun observeVpnState(): Flow<VpnState>
    suspend fun updateVpnState(state: VpnState)
}
