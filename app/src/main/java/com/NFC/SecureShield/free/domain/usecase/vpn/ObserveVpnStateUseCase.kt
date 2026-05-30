package com.NFC.SecureShield.free.domain.usecase.vpn

import com.NFC.SecureShield.free.domain.model.VpnState
import com.NFC.SecureShield.free.domain.repository.VpnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveVpnStateUseCase @Inject constructor(
    private val repository: VpnRepository
) {
    operator fun invoke(): Flow<VpnState> = repository.observeVpnState()
}
