package com.nfc.security.domain.usecase.vpn

import com.nfc.security.domain.model.VpnState
import com.nfc.security.domain.repository.VpnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveVpnStateUseCase @Inject constructor(
    private val repository: VpnRepository
) {
    operator fun invoke(): Flow<VpnState> = repository.observeVpnState()
}
