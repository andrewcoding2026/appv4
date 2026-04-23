package com.nfc.security.ui.vpn

import com.nfc.security.domain.model.VpnState

data class VpnUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val isLoading: Boolean = false
)
