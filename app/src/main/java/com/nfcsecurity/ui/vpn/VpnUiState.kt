package com.nfcsecurity.ui.vpn

import com.nfcsecurity.domain.model.VpnState

data class VpnUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val isLoading: Boolean = false
)
