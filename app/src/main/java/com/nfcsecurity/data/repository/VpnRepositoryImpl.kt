package com.nfcsecurity.data.repository

import com.nfcsecurity.domain.model.VpnState
import com.nfcsecurity.domain.repository.VpnRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VpnRepositoryImpl @Inject constructor() : VpnRepository {

    private val _state = MutableStateFlow<VpnState>(VpnState.Disconnected)

    override fun observeVpnState(): Flow<VpnState> = _state.asStateFlow()

    override suspend fun updateVpnState(state: VpnState) {
        _state.value = state
    }
}
