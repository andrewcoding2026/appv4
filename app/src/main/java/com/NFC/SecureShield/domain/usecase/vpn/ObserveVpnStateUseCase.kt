package com.NFC.SecureShield.domain.usecase.vpn

import com.NFC.SecureShield.domain.model.VpnState
import com.NFC.SecureShield.domain.repository.VpnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveVpnStateUseCase @Inject constructor(
    private val repository: VpnRepository
) {
    operator fun invoke(): Flow<VpnState> = repository.observeVpnState()
}
