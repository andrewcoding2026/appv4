package com.nfcsecurity.domain.usecase.vpn

import com.nfcsecurity.domain.model.VpnState
import com.nfcsecurity.domain.repository.VpnRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveVpnStateUseCase @Inject constructor(
    private val repository: VpnRepository
) {
    operator fun invoke(): Flow<VpnState> = repository.observeVpnState()
}
